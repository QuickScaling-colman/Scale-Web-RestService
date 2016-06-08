import React from 'react';
import WebSocket from 'ws';
import {AppBar} from 'material-ui';
import Websites from '../components/websites.jsx';
import Preview from '../components/preview.jsx'

export default class Index extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div style={{backgroundColor:'#dae5eb'}}>
        <AppBar showMenuIconButton={false} title="QuickScaling" style={{backgroundColor:'#21596d'}}/>
        <div style={{width:'100%',height:'80vh'}}>
          <Preview />
        </div>
        <div style={{width:'90%',margin:'auto'}}>
          <Websites />
        </div>
      </div>
    );
  }
}
