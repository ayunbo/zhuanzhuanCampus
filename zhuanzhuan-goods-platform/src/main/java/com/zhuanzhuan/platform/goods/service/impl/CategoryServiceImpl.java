package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.CategoryConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PageConstant;
import com.zhuanzhuan.constant.StatusConstant;
import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.dto.CategorySaveDTO;
import com.zhuanzhuan.dto.CategorySortDTO;
import com.zhuanzhuan.dto.CategoryStatusDTO;
import com.zhuanzhuan.dto.CategoryUpdateDTO;
import com.zhuanzhuan.entity.Category;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.goods.mapper.CategoryMapper;
import com.zhuanzhuan.platform.goods.service.CategoryService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.CategoryTreeVO;
import com.zhuanzhuan.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类业务实现。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类。
     */
    @Override
    public void createCategory(CategorySaveDTO categorySaveDTO) {
        // 1、校验新增参数
        if (categorySaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理新增字段
        String name = categorySaveDTO.getName();
        if (StringUtils.hasText(name)) {
            name = name.trim();
        } else {
            name = null;
        }

        Long parentId = categorySaveDTO.getParentId();
        if (parentId == null) {
            parentId = CategoryConstant.ROOT_PARENT_ID;
        }

        Integer status = categorySaveDTO.getStatus();
        if (status == null) {
            status = StatusConstant.ENABLE;
        }

        Integer sort = categorySaveDTO.getSort();
        if (sort == null) {
            sort = CategoryConstant.DEFAULT_SORT;
        }

        // 3、校验分类基础信息
        if (!StringUtils.hasText(name)) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_EMPTY);
        }
        if (parentId < 0) {
            throw new BaseException(MessageConstant.CATEGORY_PARENT_ID_INVALID);
        }
        if (!isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 4、校验父分类和同级重名，分类树不允许同一父节点下出现重复名称
        int level = resolveLevel(parentId);

        int duplicatedCount = categoryMapper.countByParentIdAndName(parentId, name);
        if (duplicatedCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        if (StatusConstant.ENABLE.equals(status) && !CategoryConstant.ROOT_PARENT_ID.equals(parentId)) {
            Category parent = categoryMapper.getById(parentId);
            if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                throw new BaseException(MessageConstant.CATEGORY_PARENT_DISABLED_FOR_CREATE);
            }
        }

        // 5、保存分类
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(level);
        category.setSort(sort);
        category.setStatus(status);

        int rows = categoryMapper.insert(category);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.CATEGORY_CREATE_FAILED);
        }
    }

    /**
     * 分页查询分类。
     */
    @Override
    public PageResult pageQueryCategory(CategoryPageQueryDTO pageQueryDTO) {
        // 1、整理分页参数
        CategoryPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new CategoryPageQueryDTO();
        }

        int page = PageConstant.DEFAULT_PAGE;
        if (queryDTO.getPage() != null) {
            page = queryDTO.getPage();
        }
        if (page < PageConstant.DEFAULT_PAGE) {
            page = PageConstant.DEFAULT_PAGE;
        }

        int pageSize = PageConstant.DEFAULT_PAGE_SIZE;
        if (queryDTO.getPageSize() != null) {
            pageSize = queryDTO.getPageSize();
        }
        if (pageSize < PageConstant.DEFAULT_PAGE) {
            pageSize = PageConstant.DEFAULT_PAGE_SIZE;
        }

        // 2、整理并校验查询条件
        if (StringUtils.hasText(queryDTO.getName())) {
            queryDTO.setName(queryDTO.getName().trim());
        } else {
            queryDTO.setName(null);
        }

        if (queryDTO.getStatus() != null && !isValidStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<CategoryVO> records = categoryMapper.pageQuery(queryDTO);
        Page<CategoryVO> pageInfo = (Page<CategoryVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询分类树。
     */
    @Override
    public List<CategoryTreeVO> listCategoryTree(boolean onlyEnable) {
        // 1、查询分类列表
        Integer status = null;
        if (onlyEnable) {
            status = StatusConstant.ENABLE;
        }
        List<Category> categories = categoryMapper.listByStatus(status);

        // 2、先构建节点映射
        Map<Long, CategoryTreeVO> categoryMap = new LinkedHashMap<>();
        for (Category category : categories) {
            CategoryTreeVO treeVO = new CategoryTreeVO();
            BeanUtils.copyProperties(category, treeVO);
            categoryMap.put(category.getId(), treeVO);
        }

        // 3、再组装父子关系
        List<CategoryTreeVO> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryTreeVO current = categoryMap.get(category.getId());
            Long parentId = category.getParentId();
            if (CategoryConstant.ROOT_PARENT_ID.equals(parentId) || !categoryMap.containsKey(parentId)) {
                roots.add(current);
            } else {
                categoryMap.get(parentId).getChildren().add(current);
            }
        }
        return roots;
    }

    /**
     * 修改分类。
     */
    @Override
    public void updateCategory(CategoryUpdateDTO categoryUpdateDTO) {
        // 1、校验修改参数
        if (categoryUpdateDTO == null || categoryUpdateDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标分类
        Category existing = categoryMapper.getById(categoryUpdateDTO.getId());
        if (existing == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 3、整理修改字段
        String name = categoryUpdateDTO.getName();
        if (StringUtils.hasText(name)) {
            name = name.trim();
        } else {
            name = null;
        }

        Long parentId = categoryUpdateDTO.getParentId();
        if (parentId == null) {
            parentId = existing.getParentId();
        }

        // 4、校验修改条件
        if (!StringUtils.hasText(name)) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_EMPTY);
        }
        if (parentId < 0 || parentId.equals(existing.getId())) {
            throw new BaseException(MessageConstant.CATEGORY_PARENT_ID_INVALID);
        }

        if (!parentId.equals(existing.getParentId())) {
            int childCount = categoryMapper.countChildrenByParentId(existing.getId());
            if (childCount > 0) {
                throw new BaseException(MessageConstant.CATEGORY_HAS_CHILDREN_CANNOT_CHANGE_PARENT);
            }
        }

        int level = resolveLevel(parentId);

        int duplicatedCount = categoryMapper.countByParentIdAndNameExcludeId(parentId, name, existing.getId());
        if (duplicatedCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        if (StatusConstant.ENABLE.equals(existing.getStatus()) && !CategoryConstant.ROOT_PARENT_ID.equals(parentId)) {
            Category parent = categoryMapper.getById(parentId);
            if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                throw new BaseException(MessageConstant.CATEGORY_PARENT_DISABLED_FOR_MOVE);
            }
        }

        // 5、执行更新
        Category updateEntity = new Category();
        updateEntity.setId(existing.getId());
        updateEntity.setParentId(parentId);
        updateEntity.setName(name);
        updateEntity.setLevel(level);
        if (categoryUpdateDTO.getSort() != null) {
            updateEntity.setSort(categoryUpdateDTO.getSort());
        }

        int rows = categoryMapper.updateByIdSelective(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.CATEGORY_UPDATE_FAILED);
        }
    }

    /**
     * 修改分类状态。
     */
    @Override
    public void updateCategoryStatus(CategoryStatusDTO categoryStatusDTO) {
        // 1、校验状态参数
        if (categoryStatusDTO == null
                || categoryStatusDTO.getId() == null
                || categoryStatusDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Integer status = categoryStatusDTO.getStatus();
        if (!isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 2、查询目标分类
        Category existing = categoryMapper.getById(categoryStatusDTO.getId());
        if (existing == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        if (status.equals(existing.getStatus())) {
            return;
        }

        // 3、启用时先校验父分类状态，避免子分类启用后挂在一个禁用父分类下
        if (StatusConstant.ENABLE.equals(status)) {
            if (!CategoryConstant.ROOT_PARENT_ID.equals(existing.getParentId())) {
                Category parent = categoryMapper.getById(existing.getParentId());
                if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                    throw new BaseException(MessageConstant.CATEGORY_PARENT_DISABLED_FOR_ENABLE);
                }
            }

            // 4、父分类可用时，只更新当前分类状态，不影响现有子分类
            Category updateEntity = new Category();
            updateEntity.setId(existing.getId());
            updateEntity.setStatus(StatusConstant.ENABLE);
            categoryMapper.updateByIdSelective(updateEntity);
            return;
        }

        // 4、禁用分类时递归禁用整棵子树，避免前台出现父禁用子可用的脏状态
        disableRecursively(existing.getId());
    }

    /**
     * 修改分类排序。
     */
    @Override
    public void updateCategorySort(CategorySortDTO categorySortDTO) {
        // 1、校验排序参数
        if (categorySortDTO == null
                || categorySortDTO.getId() == null
                || categorySortDTO.getSort() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标分类
        Category existing = categoryMapper.getById(categorySortDTO.getId());
        if (existing == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 3、执行排序更新
        Category updateEntity = new Category();
        updateEntity.setId(existing.getId());
        updateEntity.setSort(categorySortDTO.getSort());

        int rows = categoryMapper.updateByIdSelective(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.CATEGORY_SORT_UPDATE_FAILED);
        }
    }

    /**
     * 删除分类。
     */
    @Override
    public void deleteCategory(Long id) {
        // 1、校验删除参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标分类
        Category existing = categoryMapper.getById(id);
        if (existing == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 3、校验是否允许删除，有子分类或仍挂着商品时都不能直接删
        int childCount = categoryMapper.countChildrenByParentId(id);
        if (childCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_HAS_CHILDREN_CANNOT_DELETE);
        }

        int goodsCount = categoryMapper.countGoodsByCategoryId(id);
        if (goodsCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_HAS_GOODS_CANNOT_DELETE);
        }

        // 4、执行删除
        int rows = categoryMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.CATEGORY_DELETE_FAILED);
        }
    }

    /**
     * 递归禁用分类分支。
     */
    private void disableRecursively(Long categoryId) {
        // 1、先禁用当前分类
        Category updateEntity = new Category();
        updateEntity.setId(categoryId);
        updateEntity.setStatus(StatusConstant.DISABLE);
        categoryMapper.updateByIdSelective(updateEntity);

        // 2、再继续处理直接子分类，确保整棵分支状态一致
        List<Category> children = categoryMapper.listByParentId(categoryId);
        for (Category child : children) {
            if (!StatusConstant.DISABLE.equals(child.getStatus())) {
                disableRecursively(child.getId());
            }
        }
    }

    /**
     * 根据父分类计算层级。
     */
    private int resolveLevel(Long parentId) {
        // 1、根分类固定为一级
        if (CategoryConstant.ROOT_PARENT_ID.equals(parentId)) {
            return CategoryConstant.ROOT_LEVEL;
        }

        // 2、普通分类必须先查到父分类，再在父级层级基础上加一
        Category parent = categoryMapper.getById(parentId);
        if (parent == null) {
            throw new BaseException(MessageConstant.CATEGORY_PARENT_NOT_FOUND);
        }

        // 3、限制最大层级，防止分类树无限向下扩展
        int level = parent.getLevel() + CategoryConstant.ROOT_LEVEL;
        if (level > CategoryConstant.MAX_LEVEL) {
            throw new BaseException(MessageConstant.CATEGORY_LEVEL_EXCEEDED);
        }
        return level;
    }

    /**
     * 校验分类状态是否合法。
     */
    private boolean isValidStatus(Integer status) {
        return StatusConstant.ENABLE.equals(status)
                || StatusConstant.DISABLE.equals(status);
    }
}
