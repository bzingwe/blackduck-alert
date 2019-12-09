import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import RoleTable from 'dynamic/loaded/users/RoleTable';
import UserTable from 'dynamic/loaded/users/UserTable';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class UserManagement extends Component {

    constructor(props) {
        super(props);

        const descriptor = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)[0];

        this.state = {
            descriptor: descriptor
        };
    }

    render() {
        const { descriptor } = this.state;
        const canCreate = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);
        return (
            <div>
                <ConfigurationLabel configurationName="User Management" description="Create, edit, or delete Users and Roles to customize what the user can do in Alert." />
                <Tabs defaultActiveKey={1} id="user-management-tabs">
                    <Tab eventKey={1} title="Users">
                        <UserTable canCreate={canCreate} canDelete={canDelete} />
                    </Tab>
                    <Tab eventKey={2} title="Roles">
                        <RoleTable canCreate={canCreate} canDelete={canDelete} />
                    </Tab>
                </Tabs>
            </div>
        );
    }
}

UserManagement.propTypes = {
    descriptors: PropTypes.array
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(UserManagement);
