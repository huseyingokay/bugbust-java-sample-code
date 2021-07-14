/*
 * Copyright 2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.eclipse.identitymanagement.group;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.amazonaws.eclipse.core.AwsToolkitCore;
import com.amazonaws.eclipse.explorer.identitymanagement.AbstractPolicyTable;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.Group;
import com.amazonaws.services.identitymanagement.model.ListGroupPoliciesRequest;

public class GroupPermissionTable extends AbstractPolicyTable {

    private Group group;

    GroupPermissionTable(AmazonIdentityManagement iam, Composite parent, FormToolkit toolkit) {
        super(iam, parent, toolkit);

        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                if (viewer.getTable().getSelectionCount() > 0) {
                    manager.add(new removePolicyAction());
                    manager.add(new ShowPolicyAction());
                    manager.add(new EditPolicyAction());
                }
            }
        });
        viewer.getTable().setMenu(menuManager.createContextMenu(viewer.getTable()));

    }

    private class removePolicyAction extends Action {
        @Override
        public ImageDescriptor getImageDescriptor() {
            return AwsToolkitCore.getDefault().getImageRegistry().getDescriptor(AwsToolkitCore.IMAGE_REMOVE);
        }

        @Override
        public void run() {
            String policyName = contentProvider.getItemByIndex(viewer.getTable().getSelectionIndex());
            String alertMessage = "Are you sure you want to remove policy '" + policyName + "' from the group '" + group.getGroupName() + "'?";
            boolean confirmation = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Remove Policy", alertMessage);
            if (confirmation) {
                deletePolicy(policyName);
                refresh();
            }
        }

        @Override
        public String getText() {
            return "Remove Policy";
        }

    }

    private class ShowPolicyAction extends Action {

        @Override
        public void run() {
            String policyName = contentProvider.getItemByIndex(viewer.getTable().getSelectionIndex());
            new ShowGroupPolicyDialog(iam, Display.getCurrent().getActiveShell(), group, policyName, false).open();
            refresh();
        }

        @Override
        public String getText() {
            return "Show Policy";
        }

    }

    private class EditPolicyAction extends Action {

        @Override
        public void run() {
            String policyName = contentProvider.getItemByIndex(viewer.getTable().getSelectionIndex());
            new ShowGroupPolicyDialog(iam, Display.getCurrent().getActiveShell(), group, policyName, true).open();
            refresh();
        }

        @Override
        public String getText() {
            return "Edit Policy";
        }

    }

    private void deletePolicy(String policyName) {
        iam.deleteGroupPolicy(new DeleteGroupPolicyRequest().withGroupName(group.getGroupName()).withPolicyName(policyName));
    }

    @Override
    protected void getPolicyNames() {
        if (group != null) {
        policyNames = iam.listGroupPolicies(new ListGroupPoliciesRequest().withGroupName(group.getGroupName())).getPolicyNames();
        } else {
            policyNames = null;
        }
    }

    public void setGroup(Group group) {
        this.group = group;
        refresh();
    }

    @Override
    public void refresh() {
        new LoadPermissionTableThread().start();
    }

}
