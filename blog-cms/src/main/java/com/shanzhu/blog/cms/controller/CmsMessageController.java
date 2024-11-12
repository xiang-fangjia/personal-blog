package com.shanzhu.blog.cms.controller;

import com.shanzhu.blog.cms.domain.CmsMessage;
import com.shanzhu.blog.cms.domain.CmsMessageLike;
import com.shanzhu.blog.cms.service.ICmsMessageService;
import com.shanzhu.blog.common.annotation.Log;
import com.shanzhu.blog.common.core.controller.BaseController;
import com.shanzhu.blog.common.core.domain.AjaxResult;
import com.shanzhu.blog.common.core.page.TableDataInfo;
import com.shanzhu.blog.common.enums.BusinessType;
import com.shanzhu.blog.common.utils.SecurityUtils;
import com.shanzhu.blog.common.utils.poi.ExcelUtil;
import com.shanzhu.blog.framework.web.service.SysPermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;


/**
 * 留言管理Controller
 * */
@RestController
@RequestMapping("/cms/message")
public class CmsMessageController extends BaseController {

    @Resource
    private ICmsMessageService cmsMessageService;

    @Resource
    private SysPermissionService permissionService;

    /**
     * 首页查询留言列表
     */
    @GetMapping("/cms/list")
    public TableDataInfo cmsList(CmsMessage cmsMessage) {
        startPage();
        List<CmsMessage> list = cmsMessageService.selectMessageList(cmsMessage);
        return getDataTable(list);
    }

    /**
     * 首页新增留言
     */
    @PostMapping("/cms/addMessage")
    public AjaxResult addMessage(@RequestBody CmsMessage cmsMessage) {
        Long parentId = cmsMessage.getParentId();
        if (parentId != null) {
            CmsMessage message = cmsMessageService.selectCmsMessageById(parentId);
            if (message.getMainId() != null) {
                cmsMessage.setMainId(message.getMainId());
            } else {
                cmsMessage.setMainId(parentId);
            }
        }
        return toAjax(cmsMessageService.insertCmsMessage(cmsMessage));
    }

    /**
     * 首页新增点赞
     */
    @PostMapping("/cms/addCmsMessageLike")
    public AjaxResult addCmsMessageLike(@RequestBody CmsMessageLike cmsMessageLike) {
        return toAjax(cmsMessageService.addCmsMessageLike(cmsMessageLike));
    }

    /**
     * 首页删除点赞
     */
    @Log(title = "删除留言点赞", businessType = BusinessType.DELETE)
    @PostMapping("/cms/delCmsMessageLike")
    public AjaxResult delCmsMessageLike(@RequestBody CmsMessageLike cmsMessageLike) {
        return toAjax(cmsMessageService.delCmsMessageLike(cmsMessageLike));
    }

    /**
     * 查询留言管理列表
     */
    @PreAuthorize("@ss.hasPermi('cms:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsMessage cmsMessage) {
        startPage();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(getLoginUser().getUser());
        if (!SecurityUtils.isAdmin(getUserId()) && !roles.contains("admin") && !roles.contains("cms")) {
            cmsMessage.setCreateBy(getUsername());
        }
        cmsMessage.setDelFlag("0");
        List<CmsMessage> list = cmsMessageService.selectCmsMessageList(cmsMessage);
        return getDataTable(list);
    }

    /**
     * 导出留言管理列表
     */
    @PreAuthorize("@ss.hasPermi('cms:message:export')")
    @Log(title = "留言管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsMessage cmsMessage) {
        List<CmsMessage> list = cmsMessageService.selectCmsMessageList(cmsMessage);
        ExcelUtil<CmsMessage> util = new ExcelUtil<CmsMessage>(CmsMessage.class);
        util.exportExcel(response, list, "留言管理数据");
    }

    /**
     * 获取留言管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('cms:message:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(cmsMessageService.selectCmsMessageById(id));
    }

    /**
     * 新增留言管理
     */
    @PreAuthorize("@ss.hasPermi('cms:message:add')")
    @Log(title = "留言管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsMessage cmsMessage) {
        return toAjax(cmsMessageService.insertCmsMessage(cmsMessage));
    }

    /**
     * 修改留言管理
     */
    @PreAuthorize("@ss.hasPermi('cms:message:edit')")
    @Log(title = "留言管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsMessage cmsMessage) {
        return toAjax(cmsMessageService.updateCmsMessage(cmsMessage));
    }

    /**
     * 删除留言管理
     */
    @PreAuthorize("@ss.hasPermi('cms:message:remove')")
    @Log(title = "留言管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cmsMessageService.deleteCmsMessageByIds(ids));
    }
}
