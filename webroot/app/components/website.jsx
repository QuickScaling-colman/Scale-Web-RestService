import React from 'react';
import {Card, CardActions, CardHeader, CardMedia, CardTitle, CardText} from 'material-ui';
import ResponseTimeComp from './response-time-comp.jsx';
import CpuMemoryComp from './cpu-memory-comp.jsx';
import GraphComponent from './graph-component.jsx';

import d3 from 'd3';

var parseDate = d3.time.format("%Y-%m-%d").parse

export default class Website extends React.Component {
  constructor (props) {
    super(props);
    this.state = {"responseTimeScale":[],"cpuMemoryScale":[]}
    this.restGetLatestData();
    var me = this;
    //setInterval(function(){ me.restGetLatestData(); }, 5000);
  }

  restGetLatestData() {
    var me = this;
    $.ajax({
      url: "/GetLatestData/300",
      dataType: 'json',
      type: 'GET',
      success: function(data) {
        me.responseTimeScale(data);
        me.cpuMemoryScale(data);
      },
      error: function(xhr, status, err) {
        console.error( status, err.toString());
        me.state.LatestData = "";
        me.forceUpdate();
      }.bind(this)
    });
  }

  cpuMemoryScale(data) {
    this.state.cpuMemoryScale = [];

    var counterScale = 0;
    var counterCPUMemory = 0;
    while(data.CpuRam.length != counterCPUMemory && data.Scale.length != counterScale) {
        var marge = Object.assign({}, data.CpuRam[counterCPUMemory], data.Scale[counterScale]);
        marge.Pcpu = marge.cpu * 100 / marge.cpu_limit

        if(marge.memory_limit == 0) {
            marge.Pram = 100;
        } else {
            marge.Pram = marge.memory * 100 / marge.memory_limit;
        }

        if(data.CpuRam[counterCPUMemory].JavaDate < data.Scale[counterScale].JavaDate) {
            marge.date = new Date(data.Scale[counterScale].JavaDate);

            counterScale++;
        } else {
            marge.date = new Date(data.CpuRam[counterCPUMemory].JavaDate);

            counterCPUMemory++;
        }

        this.state.cpuMemoryScale.push(marge);
    }

    if(counterCPUMemory != data.CpuRam.length) {
        while(data.CpuRam.length != counterCPUMemory) {
            var marge = Object.assign({}, data.ResponseTime[counterCPUMemory], data.Scale[data.Scale.length - 1]);
            marge.Pcpu = marge.cpu * 100 / marge.cpu_limit

            if(marge.memory_limit == 0) {
                marge.Pram = 100;
            } else {
                marge.Pram = marge.memory * 100 / marge.memory_limit;
            }
            marge.date = new Date(data.CpuRam[counterCPUMemory].JavaDate);
            this.state.cpuMemoryScale.push(marge);
            counterCPUMemory++;
        }

    }
    //
    // if(data.Scale.length != counterScale) {
    //     while(data.Scale.length != counterScale) {
    //         var marge = Object.assign({}, data.CpuRam[data.CpuRam.length - 1], data.Scale[counterScale]);
    //         marge.Pcpu = marge.cpu * 100 / marge.cpu_limit
    //
    //         if(marge.memory_limit == 0) {
    //             marge.Pram = 100;
    //         } else {
    //             marge.Pram = marge.memory * 100 / marge.memory_limit;
    //         }
    //         marge.date = new Date(data.Scale[counterScale].JavaDate);
    //         this.state.cpuMemoryScale.push(marge);
    //          counterScale++;
    //     }
    //
    // }

    this.setState({"cpuMemoryScale":this.state.cpuMemoryScale.reverse()});
  }

  responseTimeScale(data) {
    this.state.responseTimeScale = [];

    var counterScale = 0;
    var counterResponseTime = 0;
    while(data.ResponseTime.length != counterResponseTime && data.Scale.length != counterScale) {
        var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[counterScale]);
        if(data.ResponseTime[counterResponseTime].JavaDate < data.Scale[counterScale].JavaDate ) {
            if(counterResponseTime != 0){
                marge.date = new Date(data.Scale[counterScale].JavaDate);
                this.state.responseTimeScale.push(marge);
            }

            counterScale++;
        } else {
            marge.date = new Date(data.ResponseTime[counterResponseTime].JavaDate);
            this.state.responseTimeScale.push(marge);
            counterResponseTime++;
        }


    }

    if(counterResponseTime != data.ResponseTime.length) {
        while(data.ResponseTime.length != counterResponseTime) {
            var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[data.Scale.length - 1]);
            marge.date = new Date(data.ResponseTime[counterResponseTime].JavaDate);
            this.state.responseTimeScale.push(marge);
            counterResponseTime++;
        }

    }


    this.setState({"responseTimeScale":this.state.responseTimeScale.reverse()});
  }

  render () {
    var data = this.state.LatestData;
    if(this.state.responseTimeScale.length == 0) {
        return (<div></div>);
    }
    return (
      <div >
        <Card>
         <CardHeader title={<h3> {"http://" + this.props.websiteData.map.HostName + ":" + this.props.websiteData.map.port + this.props.websiteData.map.path.split("?")[0]} </h3>} />
         <CardText>
           {
          //  <GraphComponent title={"ResponseTime"}
          //                  GraphType={"Area"}
          //                  Data={this.state.responseTimeScale}
          //                  width={950}
          //                  height={300}
          //                  chartSeries={[{field: 'responseTime',name: 'responseTime',color: '#FFC743'},{field: 'replicas',name: 'replicas',color: '#FFC743'}]}
          //                  />
           }


           <ResponseTimeComp width={window.innerWidth} responseTimeScale={this.state.responseTimeScale}/>

           <CpuMemoryComp width={window.innerWidth} cpuMemoryScale={this.state.cpuMemoryScale}/>           
         </CardText>
       </Card>
      </div>
    );
  }
}
