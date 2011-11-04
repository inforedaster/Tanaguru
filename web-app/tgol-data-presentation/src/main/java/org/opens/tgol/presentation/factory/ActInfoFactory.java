/*
 * Tanaguru - Automated webpage assessment
 * Copyright (C) 2008-2011  Open-S Company
 *
 * This file is part of Tanaguru.
 *
 * Tanaguru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: open-s AT open-s DOT com
 */
package org.opens.tgol.presentation.factory;

import org.opens.tgol.entity.contract.Act;
import org.opens.tgol.entity.decorator.tanaguru.subject.WebResourceDataServiceDecorator;
import org.opens.tgol.presentation.data.ActInfo;
import org.opens.tgol.presentation.data.ActInfoImpl;
import org.opens.tgol.util.TgolKeyStore;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.opens.tanaguru.entity.audit.Audit;
import org.opens.tanaguru.entity.audit.AuditStatus;
import org.opens.tanaguru.entity.service.audit.ContentDataService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jkowalczyk
 */
public final class ActInfoFactory {

    private ContentDataService contentDataService;
    @Autowired
    public void setContentDataService(ContentDataService contentDataService) {
        this.contentDataService = contentDataService;
    }

    private WebResourceDataServiceDecorator webResourceDataService;
    @Autowired
    public void setWebResourceDataService(WebResourceDataServiceDecorator webResourceDataServiceDecorator) {
        this.webResourceDataService = webResourceDataServiceDecorator;
    }

    /**
     * The unique shared instance of ActInfoFactory
     */
    private static ActInfoFactory actInfoFactory;

    /**
     * Default private constructor
     */
    private ActInfoFactory(){}

    public static synchronized ActInfoFactory getInstance(){
        if (actInfoFactory == null) {
            actInfoFactory = new ActInfoFactory();
        }
        return actInfoFactory;
    }

    public ActInfo getActInfo(Act act){
        ActInfoImpl actInfo = new ActInfoImpl();
        actInfo.setDate(act.getEndDate());
        if (act.getWebResource() != null) {
            actInfo.setUrl(act.getWebResource().getURL());
            actInfo.setWebresourceId(Float.valueOf(act.getWebResource().getId()).intValue());
            actInfo.setScope(act.getScope().getCode().name());
            Audit audit = act.getWebResource().getAudit();
            if (audit.getStatus().equals(AuditStatus.COMPLETED)) {
                actInfo.setWeightedMark(webResourceDataService.getMarkByWebResourceAndAudit(act.getWebResource(), audit, false).intValue());
                actInfo.setRawMark(webResourceDataService.getMarkByWebResourceAndAudit(act.getWebResource(), audit, true).intValue());
                actInfo.setStatus(TgolKeyStore.COMPLETED_KEY);
            } else if (!contentDataService.hasContent(audit)){
                actInfo.setStatus(TgolKeyStore.ERROR_LOADING_KEY);
            } else if (!contentDataService.hasAdaptedSSP(audit)) {
                actInfo.setStatus(TgolKeyStore.ERROR_ADAPTING_KEY);
            } else {
                actInfo.setStatus(TgolKeyStore.ERROR_UNKNOWN_KEY);
            }
        }

        return actInfo;
    }

    public Collection<ActInfo> getActInfoSet(Collection<Act> actSet){
        Set<ActInfo> actInfoSet = new LinkedHashSet<ActInfo>();
        for (Act act : actSet) {
            actInfoSet.add(getActInfo(act));
        }
        return actInfoSet;
    }

}