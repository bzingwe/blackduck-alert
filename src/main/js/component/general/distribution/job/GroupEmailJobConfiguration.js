import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import Select from 'react-select-2';
import TextInput from '../../../../field/input/TextInput';
import BaseJobConfiguration from './BaseJobConfiguration';
import {getDistributionJob} from '../../../../store/actions/distributions';


class GroupEmailJobConfiguration extends Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleStateValues = this.handleStateValues.bind(this);
        this.handleGroupsChanged = this.handleGroupsChanged.bind(this);
        this.getConfiguration = this.getConfiguration.bind(this);

        this.state = {
            emailSubjectLine: props.emailSubjectLine,
            groupName: props.groupName,
            groupOptions: []
        }
    }

    createGroupOptions(groups, groupName) {
        if(groups) {
            const groupOptions = groups.map(group => ({
                label: group.name,
                value: group.name,
                missing: false
            }))
            .sort((group1, group2) => {
                    if (group1.value < group2.value) {
                    return -1;
                } else if (group1.value > group2.value) {
                    return 1;
                }
                return 0;
            });

            const groupFound = groupOptions.find(group => group.label === groupName);
            if (groupName && (!groupFound || groupOptions.length === 0)) {
                groupOptions.push({
                    label: groupName,
                    value: groupName,
                    missing: true
                });
            }
            return groupOptions;

        } else {
            return [];
        }
    }

    componentDidMount() {

        console.log("Group email mounted props", this.props);
        const {baseUrl,distributionConfigId} = this.props;
        this.props.getDistributionJob(baseUrl,distributionConfigId);
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching && !nextProps.inProgress) {
            if(nextProps.jobs[nextProps.distributionConfigId]) {
                const groupOptions = this.createGroupOptions(nextProps.groups, nextProps[nextProps.distributionConfigId].groupName);
                this.setState({
                    emailSubjectLine: nextProps.jobs[nextProps.distributionConfigId].emailSubjectLine,
                    groupName: nextProps.jobs[nextProps.distributionConfigId].groupName,
                    groupOptions: groupOptions
                });
            }
        }
    }

    handleChange({target}) {
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const {name} = target;
        this.handleStateValues(name, value);
    }

    handleStateValues(name, value) {
        this.setState({
            [name]: value
        });
    }

    getConfiguration() {
        return Object.assign({}, this.state, {
            distributionType: this.props.distributionType
        });
    }

    handleGroupsChanged(optionsList) {
        if (optionsList) {
            this.handleStateValues('groupName', optionsList.value);
        } else {
            this.handleStateValues('groupName', null);
        }
    }

    // initializeValues(data) {
    //     super.initializeValues(data);
    //     const groupName = data.groupName || this.props.groupName;
    //     const emailSubjectLine = data.emailSubjectLine || this.props.emailSubjectLine;
    //
    //     const groupOptions = this.props.groups.map(group => ({
    //         label: group.name,
    //         value: group.name,
    //         missing: false
    //     })).sort((group1, group2) => {
    //         if (group1.value < group2.value) {
    //             return -1;
    //         } else if (group1.value > group2.value) {
    //             return 1;
    //         }
    //         return 0;
    //     });
    //
    //     const groupFound = groupOptions.find(group => group.label === groupName);
    //     if (groupName && (!groupFound || groupOptions.length === 0)) {
    //         groupOptions.push({
    //             label: groupName,
    //             value: groupName,
    //             missing: true
    //         });
    //     }
    //
    //     this.state.groupOptions = groupOptions;
    //     super.handleStateValues('groupName', groupName);
    //     super.handleStateValues('emailSubjectLine', emailSubjectLine);
    // }

    renderOption(option) {
        if (option.missing) {
            return (
                <span className="missingBlackDuckData"><span className="fa fa-exclamation-triangle fa-fw" aria-hidden="true"/>{option.label} (Group not found on Black Duck server)</span>
            );
        }

        return (
            <span>{option.label}</span>
        );
    }

    render() {
        const {groupOptions} = this.state;
        const {groupName} = this.state;
        const options = groupOptions || [];

        const content = (
            <div>
                <TextInput id="jobEmailSubject" label="Subject Line" name="emailSubjectLine" value={this.state.emailSubjectLine} onChange={this.handleChange} errorName="emailSubjectLineError"
                    errorValue={this.props.emailSubjectLineError}/>

                <div className="form-group">
                    <label className="col-sm-3 control-label">Group</label>
                    <div className="col-sm-8">
                    <Select
                    id="jobEmailGroup"
                    className="typeAheadField"
                    onChange={this.handleGroupsChanged}
                    clearable
                    options={options}
                    optionRenderer={this.renderOption}
                    placeholder="Choose the Black Duck user group"
                    value={groupName}
                    valueRenderer={this.renderOption}
                    searchable
                    />

                    {this.props.errors.groupNameError && <label className="fieldError" name="groupError">
                        {this.props.errors.groupNameError}
                    </label>}
                    </div>
                </div>
                {this.props.waitingForGroups && <div className="inline">
                    <span className="fa fa-spinner fa-pulse fa-fw" aria-hidden/>
                </div>}
            </div>);
        return (<BaseJobConfiguration
                    baseUrl={this.props.baseUrl}
                    testUrl={this.props.testUrl}
                    id = {this.props.id}
                    distributionConfigId = {this.props.distributionConfigId}
                    handleCancel={this.props.handleCancel}
                    handleSaveBtnClick={this.props.handleSaveBtnClick}
                    getParentConfiguration={this.getConfiguration}
                    childContent={content} />);
    }
}

GroupEmailJobConfiguration.propTypes = {
    jobs: PropTypes.object,
    id: PropTypes.string,
    distributionConfigId: PropTypes.string,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string,
    distributionType: PropTypes.string,
    getDistributionJob: PropTypes.func.isRequired,
    csrfToken: PropTypes.string,
    groups: PropTypes.arrayOf(PropTypes.object),
    errors: PropTypes.object,
    handleCancel: PropTypes.func.isRequired,
    handleSaveBtnClick: PropTypes.func.isRequired

};

GroupEmailJobConfiguration.defaultProps = {
    jobs: {},
    id: null,
    distributionConfigId: null,
    baseUrl: '/alert/api/configuration/channel/distribution/channel_email',
    testUrl: '/alert/api/configuration/channel/distribution/channel_email/test',
    distributionType: 'channel_email',
    emailSubjectLine: '',
    groupName: '',
    groups: [],
    errors: {}
};

const mapDispatchToProps = dispatch => ({
    getDistributionJob: (url,id) => dispatch(getDistributionJob(url,id))
});

const mapStateToProps = state => ({
    jobs: state.distributions.jobs,
    errors: state.distributions.errors
});

export default connect(mapStateToProps, mapDispatchToProps)(GroupEmailJobConfiguration);
