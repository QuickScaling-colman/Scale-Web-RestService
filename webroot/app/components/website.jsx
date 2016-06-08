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
    this.state = {"responseTimeScale":[],"cpuMemoryScale":[],"ws": new WebSocket("ws://localhost:8002/")}
    this.restGetLatestData();
    var me = this;
    //setInterval(function(){ me.restGetLatestData(); }, 5000);
  }

  websocketInit () {
      var me = this;
      var ws = this.state.ws;
      ws.addEventListener('open', function open() {});
      ws.addEventListener('message', function incoming(event) {
        var data = JSON.parse(event.data);
        me.responseTimeScale(data);
        me.cpuMemoryScale(data);
      });
      ws.addEventListener('close', function close() {});
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
        me.websocketInit();
      },
      error: function(xhr, status, err) {
        console.error( status, err.toString());
        me.state.LatestData = "";
        me.forceUpdate();
      }.bind(this)
    });
  }

  cpuMemoryScale(data) {
    if(data.CpuRam.length == 0){
        if(this.state.cpuMemoryScale.length == 0){
            this.state.cpuMemoryScale.push({"date":new Date(),"Pcpu":0,"Pram":0});
            this.setState({"cpuMemoryScale":this.state.cpuMemoryScale});
        }


        return;
    }

    var counterScale = data.Scale.length - 1;
    var counterCPUMemory = data.CpuRam.length - 1;
    while(counterCPUMemory != 0 && counterScale != 0) {
        var marge = Object.assign({}, data.CpuRam[counterCPUMemory], data.Scale[counterScale]);
        marge.Pcpu = marge.cpu * 100 / marge.cpu_limit;

        if(marge.memory_limit == 0) {
            marge.Pram = 100;
        } else {
            marge.Pram = marge.memory * 100 / marge.memory_limit;
        }

        if(data.CpuRam[counterCPUMemory].JavaDate > data.Scale[counterScale].JavaDate) {
            marge.date = new Date(data.Scale[counterScale].JavaDate);
            marge.JavaDate = data.Scale[counterScale].JavaDate;

            counterScale--;
        } else {
            marge.date = new Date(data.CpuRam[counterCPUMemory].JavaDate);
            marge.JavaDate = data.CpuRam[counterCPUMemory].JavaDate;

            counterCPUMemory--;
        }
        if(this.state.cpuMemoryScale.length == 0 || this.state.cpuMemoryScale[this.state.cpuMemoryScale.length - 1].JavaDate < marge.JavaDate) {
            this.state.cpuMemoryScale.push(marge);
        }

    }


      while(counterCPUMemory != 0) {
          var marge = Object.assign({}, data.ResponseTime[counterCPUMemory], data.Scale[data.Scale.length - 1]);
          marge.Pcpu = marge.cpu * 100 / marge.cpu_limit

          if(marge.memory_limit == 0) {
              marge.Pram = 100;
          } else {
              marge.Pram = marge.memory * 100 / marge.memory_limit;
          }
          marge.date = new Date(data.CpuRam[counterCPUMemory].JavaDate);
          marge.JavaDate = data.CpuRam[counterCPUMemory].JavaDate;

          if(this.state.cpuMemoryScale.length == 0 || this.state.cpuMemoryScale[this.state.cpuMemoryScale.length - 1].JavaDate < marge.JavaDate) {
            this.state.cpuMemoryScale.push(marge);
          }

          counterCPUMemory--;
      }



      this.setState({"cpuMemoryScale":this.state.cpuMemoryScale});
  }

  responseTimeScale(data) {
    if(data.ResponseTime.length == 0){
        return;
    }

    var counterScale = data.Scale.length - 1;
    var counterResponseTime = data.ResponseTime.length - 1;
    while(counterResponseTime != 0 && counterScale != 0) {
        var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[counterScale]);
        if(data.ResponseTime[counterResponseTime].JavaDate < data.Scale[counterScale].JavaDate ) {
            if(counterResponseTime != 0){
                marge.date = new Date(data.Scale[counterScale].JavaDate);
                marge.JavaDate = data.Scale[counterScale].JavaDate;
            }

            counterScale--;
        } else {
            marge.date = new Date(data.ResponseTime[counterResponseTime].JavaDate);
            marge.JavaDate = data.ResponseTime[counterResponseTime].JavaDate;
            counterResponseTime--;
        }

        if(this.state.responseTimeScale.length == 0 || this.state.responseTimeScale[this.state.responseTimeScale.length - 1].JavaDate < marge.JavaDate) {
          this.state.responseTimeScale.push(marge);
        }
    }


      while(counterResponseTime != 0) {
          var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[data.Scale.length - 1]);
          marge.date = new Date(data.ResponseTime[counterResponseTime].JavaDate);
          marge.JavaDate = data.ResponseTime[counterResponseTime].JavaDate;

          if(this.state.responseTimeScale.length == 0 || this.state.responseTimeScale[this.state.responseTimeScale.length - 1].JavaDate < marge.JavaDate) {
            this.state.responseTimeScale.push(marge);
          }
          counterResponseTime--;
      }




      this.setState({"responseTimeScale":this.state.responseTimeScale});
  }

  render () {
    var data = this.state.LatestData;

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


           <ResponseTimeComp width={window.innerWidth * 0.89} height={window.innerHeight * 0.32} responseTimeScale={this.state.responseTimeScale}/>

           <CpuMemoryComp width={window.innerWidth * 0.89} height={window.innerHeight * 0.32} cpuMemoryScale={this.state.cpuMemoryScale}/>
         </CardText>
       </Card>
      </div>
    );
  }
}
