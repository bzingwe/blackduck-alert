import React, { Component } from 'react';
import CollapsiblePane from "component/common/CollapsiblePane";
import { connect } from 'react-redux';
import ConfigurationLabel from "../../../component/common/ConfigurationLabel";

class UserManagement extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const expanded = true;
        return (
            <div>
                <ConfigurationLabel configurationName="User Management" />
                <CollapsiblePane
                    title='Users'
                    expanded={expanded}
                >
                </CollapsiblePane>
                <CollapsiblePane
                    title='Roles'
                    expanded={expanded}
                >
                </CollapsiblePane>
            </div>
        );
    }
}

UserManagement.defaultProps = {};

UserManagement.propTypes = {};

const mapStateToProps = () => ({});

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(UserManagement);
