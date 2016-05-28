import React from 'react';
import Website from './website.jsx'

export default class Websites extends React.Component {
  constructor (props) {
    super(props);

    this.state = {"Websites":[]};
    this.restAPI();
  }

  restAPI() {
    let me = this;
    $.ajax({
      url: "/GetAllWebsites",
      dataType: 'json',
      type: 'GET',
      success: function(data) {
        me.setState({"Websites":data});
      },
      error: function(xhr, status, err) {
        console.error( status, err.toString());
        me.state.Websites = [];
        me.forceUpdate();
      }.bind(this)
    });
  }

  render () {
    let WebsitesComp = [];

    if(this.state.Websites.length > 0) {
        for(let website of this.state.Websites) {
            WebsitesComp.push(<Website key={"http://" + website.map.HostName + ":" + website.map.port + website.map.path} websiteData={website}/>)
        }

    }

    return (
      <div className={"container-fluid"} style={{marginTop:'15px'}}>
        <div className={"row"}>
          <div className={"col-md-12"}>
            {WebsitesComp}
          </div>
        </div>
      </div>
    );
  }
}
