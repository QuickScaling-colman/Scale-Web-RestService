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
      url: "http://localhost:8080/GetAllWebsites",
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
            WebsitesComp.push(<Website key={website.map.URL} websiteData={website}/>)
        }

    }

    return (
      <div className={"container-fluid"} style={{marginTop:'15px'}}>
        <div className={"row"}>
          <div className={"col-md-4"}>
            {WebsitesComp}
          </div>
        </div>
      </div>
    );
  }
}
