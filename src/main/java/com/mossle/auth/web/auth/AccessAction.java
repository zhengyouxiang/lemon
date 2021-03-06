package com.mossle.auth.web.auth;

import java.util.ArrayList;
import java.util.List;

import com.mossle.api.ScopeConnector;

import com.mossle.auth.domain.Access;
import com.mossle.auth.domain.Perm;
import com.mossle.auth.manager.AccessManager;
import com.mossle.auth.manager.PermManager;

import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.hibernate.PropertyFilter;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.scope.ScopeHolder;
import com.mossle.core.struts2.BaseAction;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;

@Results({ @Result(name = AccessAction.RELOAD, location = "access.do?operationMode=RETRIEVE&appId=${appId}", type = "redirect") })
public class AccessAction extends BaseAction implements ModelDriven<Access>,
        Preparable {
    public static final String RELOAD = "reload";
    private AccessManager accessManager;
    private MessageSourceAccessor messages;
    private Page page = new Page();
    private Access model;
    private long id;
    private List<Long> selectedItem = new ArrayList<Long>();
    private Exportor exportor = new Exportor();
    private BeanMapper beanMapper = new BeanMapper();
    private List<Perm> perms;
    private PermManager permManager;
    private Long permId;
    private ScopeConnector scopeConnector;

    public String execute() {
        return list();
    }

    public String list() {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromHttpRequest(ServletActionContext.getRequest());
        Long localId = scopeConnector.findLocalId(ScopeHolder.getGlobalCode(),
                ScopeHolder.getLocalCode());
        propertyFilters.add(new PropertyFilter("EQL_localId", Long
                .toString(localId)));
        page = accessManager.pagedQuery(page, propertyFilters);

        return SUCCESS;
    }

    // ~ ======================================================================
    public void prepareSave() {
        model = new Access();
    }

    public String save() {
        // copy
        Access dest = null;

        if (id > 0) {
            dest = accessManager.get(id);
            beanMapper.copy(model, dest);
        } else {
            dest = model;
        }

        // foreign
        Perm perm = permManager.get(permId);
        dest.setPerm(perm);

        if (id == 0) {
            dest.setGlobalId(scopeConnector.findGlobalId(ScopeHolder
                    .getGlobalCode()));
            dest.setLocalId(scopeConnector.findLocalId(
                    ScopeHolder.getGlobalCode(), ScopeHolder.getLocalCode()));
        }

        // save
        accessManager.save(dest);

        addActionMessage(messages.getMessage("core.success.save", "保存成功"));

        return RELOAD;
    }

    public String removeAll() {
        List<Access> accesses = accessManager.findByIds(selectedItem);
        accessManager.removeAll(accesses);
        addActionMessage(messages.getMessage("core.success.delete", "删除成功"));

        return RELOAD;
    }

    public String input() {
        if (id > 0) {
            model = accessManager.get(id);
        }

        Long localId = scopeConnector.findLocalId(ScopeHolder.getGlobalCode(),
                ScopeHolder.getLocalCode());
        perms = permManager.findBy("localId", localId);

        return INPUT;
    }

    public void exportExcel() throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromHttpRequest(ServletActionContext.getRequest());
        page = accessManager.pagedQuery(page, propertyFilters);

        List<Access> accesses = (List<Access>) page.getResult();
        TableModel tableModel = new TableModel();
        tableModel.setName("access");
        tableModel.addHeaders("id", "type", "value", "perm.name", "priority",
                "app.name");
        tableModel.setData(accesses);
        exportor.exportExcel(ServletActionContext.getResponse(), tableModel);
    }

    // ~ ======================================================================
    public void prepare() {
    }

    public Access getModel() {
        return model;
    }

    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    public void setPermManager(PermManager permManager) {
        this.permManager = permManager;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    // ~ ======================================================================
    public void setId(long id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setSelectedItem(List<Long> selectedItem) {
        this.selectedItem = selectedItem;
    }

    public List<Perm> getPerms() {
        return perms;
    }

    public void setPermId(Long permId) {
        this.permId = permId;
    }

    public void setScopeConnector(ScopeConnector scopeConnector) {
        this.scopeConnector = scopeConnector;
    }
}
