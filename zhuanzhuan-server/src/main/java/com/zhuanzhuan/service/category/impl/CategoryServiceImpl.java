package com.zhuanzhuan.service.category.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.StatusConstant;
import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.dto.CategorySaveDTO;
import com.zhuanzhuan.dto.CategorySortDTO;
import com.zhuanzhuan.dto.CategoryStatusDTO;
import com.zhuanzhuan.dto.CategoryUpdateDTO;
import com.zhuanzhuan.entity.Category;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.category.CategoryMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.category.CategoryService;
import com.zhuanzhuan.utils.IdGenerator;
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

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Long ROOT_PARENT_ID = 0L;
    private static final int MAX_LEVEL = 3;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void createCategory(CategorySaveDTO categorySaveDTO) {
        if (categorySaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        String name = trimToNull(categorySaveDTO.getName());
        if (!StringUtils.hasText(name)) {
            throw new BaseException("分类名称不能为空");
        }

        Long parentId = categorySaveDTO.getParentId() == null ? ROOT_PARENT_ID : categorySaveDTO.getParentId();
        if (parentId < 0) {
            throw new BaseException("父分类ID不合法");
        }

        int level = resolveLevel(parentId);
        int duplicatedCount = categoryMapper.countByParentIdAndName(parentId, name);
        if (duplicatedCount > 0) {
            throw new BaseException("同级分类名称已存在");
        }

        Integer status = categorySaveDTO.getStatus();
        if (status == null) {
            status = StatusConstant.ENABLE;
        } else if (!isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        if (StatusConstant.ENABLE.equals(status) && !ROOT_PARENT_ID.equals(parentId)) {
            Category parent = categoryMapper.getById(parentId);
            if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                throw new BaseException("父分类未启用，不能新增启用状态的子分类");
            }
        }

        Integer sort = categorySaveDTO.getSort() == null ? 0 : categorySaveDTO.getSort();

        Category category = new Category();
        category.setId(IdGenerator.nextId());
        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(level);
        category.setSort(sort);
        category.setStatus(status);

        int rows = categoryMapper.insert(category);
        if (rows <= 0) {
            throw new BaseException("新增分类失败");
        }
    }

    @Override
    public PageResult pageQueryCategory(CategoryPageQueryDTO pageQueryDTO) {
        CategoryPageQueryDTO queryDTO = pageQueryDTO == null ? new CategoryPageQueryDTO() : pageQueryDTO;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        queryDTO.setName(trimToNull(queryDTO.getName()));
        if (queryDTO.getStatus() != null && !isValidStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        PageHelper.startPage(page, pageSize);
        List<CategoryVO> records = categoryMapper.pageQuery(queryDTO);
        Page<CategoryVO> pageInfo = (Page<CategoryVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public List<CategoryTreeVO> listCategoryTree(boolean onlyEnable) {
        Integer status = onlyEnable ? StatusConstant.ENABLE : null;
        List<Category> categories = categoryMapper.listByStatus(status);

        Map<Long, CategoryTreeVO> categoryMap = new LinkedHashMap<>();
        for (Category category : categories) {
            CategoryTreeVO treeVO = new CategoryTreeVO();
            BeanUtils.copyProperties(category, treeVO);
            categoryMap.put(category.getId(), treeVO);
        }

        List<CategoryTreeVO> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryTreeVO current = categoryMap.get(category.getId());
            Long parentId = category.getParentId();
            if (ROOT_PARENT_ID.equals(parentId) || !categoryMap.containsKey(parentId)) {
                roots.add(current);
            } else {
                categoryMap.get(parentId).getChildren().add(current);
            }
        }
        return roots;
    }

    @Override
    public void updateCategory(CategoryUpdateDTO categoryUpdateDTO) {
        if (categoryUpdateDTO == null || categoryUpdateDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Category existing = categoryMapper.getById(categoryUpdateDTO.getId());
        if (existing == null) {
            throw new BaseException("分类不存在");
        }

        String name = trimToNull(categoryUpdateDTO.getName());
        if (!StringUtils.hasText(name)) {
            throw new BaseException("分类名称不能为空");
        }

        Long parentId = categoryUpdateDTO.getParentId() == null ? existing.getParentId() : categoryUpdateDTO.getParentId();
        if (parentId < 0 || parentId.equals(existing.getId())) {
            throw new BaseException("父分类ID不合法");
        }

        if (!parentId.equals(existing.getParentId())) {
            int childCount = categoryMapper.countChildrenByParentId(existing.getId());
            if (childCount > 0) {
                throw new BaseException("存在子分类，不允许修改父分类");
            }
        }

        int level = resolveLevel(parentId);
        int duplicatedCount = categoryMapper.countByParentIdAndNameExcludeId(parentId, name, existing.getId());
        if (duplicatedCount > 0) {
            throw new BaseException("同级分类名称已存在");
        }

        if (StatusConstant.ENABLE.equals(existing.getStatus()) && !ROOT_PARENT_ID.equals(parentId)) {
            Category parent = categoryMapper.getById(parentId);
            if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                throw new BaseException("父分类未启用，不能挂载启用状态的子分类");
            }
        }

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
            throw new BaseException("更新分类失败");
        }
    }

    @Override
    public void updateCategoryStatus(CategoryStatusDTO categoryStatusDTO) {
        if (categoryStatusDTO == null || categoryStatusDTO.getId() == null || categoryStatusDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Integer status = categoryStatusDTO.getStatus();
        if (!isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        Category existing = categoryMapper.getById(categoryStatusDTO.getId());
        if (existing == null) {
            throw new BaseException("分类不存在");
        }

        if (status.equals(existing.getStatus())) {
            return;
        }

        if (StatusConstant.ENABLE.equals(status)) {
            if (!ROOT_PARENT_ID.equals(existing.getParentId())) {
                Category parent = categoryMapper.getById(existing.getParentId());
                if (parent == null || !StatusConstant.ENABLE.equals(parent.getStatus())) {
                    throw new BaseException("父分类未启用，不能单独启用当前分类");
                }
            }

            Category updateEntity = new Category();
            updateEntity.setId(existing.getId());
            updateEntity.setStatus(StatusConstant.ENABLE);
            categoryMapper.updateByIdSelective(updateEntity);
            return;
        }

        disableRecursively(existing.getId());
    }

    @Override
    public void updateCategorySort(CategorySortDTO categorySortDTO) {
        if (categorySortDTO == null || categorySortDTO.getId() == null || categorySortDTO.getSort() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Category existing = categoryMapper.getById(categorySortDTO.getId());
        if (existing == null) {
            throw new BaseException("分类不存在");
        }

        Category updateEntity = new Category();
        updateEntity.setId(existing.getId());
        updateEntity.setSort(categorySortDTO.getSort());

        int rows = categoryMapper.updateByIdSelective(updateEntity);
        if (rows <= 0) {
            throw new BaseException("更新分类排序失败");
        }
    }

    @Override
    public void deleteCategory(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Category existing = categoryMapper.getById(id);
        if (existing == null) {
            throw new BaseException("分类不存在");
        }

        int childCount = categoryMapper.countChildrenByParentId(id);
        if (childCount > 0) {
            throw new BaseException("存在子分类，不能删除");
        }

        int goodsCount = categoryMapper.countGoodsByCategoryId(id);
        if (goodsCount > 0) {
            throw new BaseException("分类下存在商品，不能删除");
        }

        int rows = categoryMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException("删除分类失败");
        }
    }

    private void disableRecursively(Long categoryId) {
        Category updateEntity = new Category();
        updateEntity.setId(categoryId);
        updateEntity.setStatus(StatusConstant.DISABLE);
        categoryMapper.updateByIdSelective(updateEntity);

        List<Category> children = categoryMapper.listByParentId(categoryId);
        for (Category child : children) {
            if (!StatusConstant.DISABLE.equals(child.getStatus())) {
                disableRecursively(child.getId());
            }
        }
    }

    private int resolveLevel(Long parentId) {
        if (ROOT_PARENT_ID.equals(parentId)) {
            return 1;
        }

        Category parent = categoryMapper.getById(parentId);
        if (parent == null) {
            throw new BaseException("父分类不存在");
        }

        int level = parent.getLevel() + 1;
        if (level > MAX_LEVEL) {
            throw new BaseException("分类层级不能超过3级");
        }
        return level;
    }

    private boolean isValidStatus(Integer status) {
        return StatusConstant.ENABLE.equals(status) || StatusConstant.DISABLE.equals(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
