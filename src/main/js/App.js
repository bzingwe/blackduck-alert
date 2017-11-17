'use strict';

import React from 'react';
import ReactDOM from 'react-dom';

import MainPage from './MainPage';
import LoginPage from './LoginPage';

import styles from '../css/main.css';


class App extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
				loggedIn: false
		};
		this.handleState = this.handleState.bind(this)
	}
	
	componentDidMount() {
		var self = this;
		fetch('/verify', {
			credentials: "same-origin"
		})  
		.then(function(response) {
			if (!response.ok) {
				self.setState({
					loggedIn: false
				});
			} else {
				self.setState({
					loggedIn: true
				});
			}
		});
	}
	
	handleState(name, value) {
		this.setState({
			[name]: value
		});
	}
	
	render() {
		let page = null;
		if (this.state.loggedIn) {
			page = <MainPage></MainPage>
		} else {
			page = <LoginPage handleState={this.handleState}></LoginPage>
		}
		return (
			<div>
				{page}
			</div>
		)
	}
}

ReactDOM.render(
		<div>
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
		<App></App>
		</div>,
		document.getElementById('react')
);