'use strict';
import React from 'react';

import styles from '../../css/field.css';

class Field extends React.Component {
	render() {
		let inputDiv = null;
		let value = this.props.value;
		if (this.props.type == 'text' || this.props.type == 'password' || this.props.type == 'number') {
			if (value == null){
				value = undefined;
			}
			inputDiv = <input className={styles.inputText} type={this.props.type} name={this.props.name} value={value} onChange={this.props.onChange}></input>
		} else if (this.props.type == 'checkbox') {
			let isChecked = false;
			if (value != undefined && value != null && (value == true || value == 'true')) {
				isChecked = true;
			}
			inputDiv = <input type={this.props.type} name={this.props.name} checked={isChecked} onChange={this.props.onChange}></input>
		}

		let errorDiv = null;
		if (this.props.errorName && this.props.errorValue) {
			errorDiv = <p name={this.props.errorName}>{this.props.errorValue}</p>;
		}
		return (
				<div>
				<label className={styles.inputLabel}>{this.props.label}</label>
				{inputDiv}
				{errorDiv}
				</div>
		)
	}
}

export default Field;