import React from 'react';
import WebSocket from 'ws';
import {AppBar} from 'material-ui';
import Websites from '../components/websites.jsx';

export default class Index extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div >
        <AppBar title="QuickScaling" />
        <Websites />
      </div>
    );
  }
}
