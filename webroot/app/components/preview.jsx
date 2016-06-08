import React from 'react';

export default class Preview extends React.Component {
  constructor (props) {
    super(props);
  }

  _onclick() {
      $("html, body").animate({ scrollTop: $(document).height()-$(window).height()},1500 );
  }

  render () {

    return (
      <div style={{background:'url(../pic/quickscalingcaver.jpg) no-repeat center top fixed',height:'100%',backgroundSize:'cover',backgroundPositionY:'15%',backgroundPositionX:0}}>
        <i style={{position:'absolute',color:'white',top:'78%',left:'50%',fontWeight:900,cursor:'pointer'}} onClick={this._onclick.bind(this)} className="fa fa-angle-double-down fa-5x down-icon"/>
      </div>
    );
  }
}
