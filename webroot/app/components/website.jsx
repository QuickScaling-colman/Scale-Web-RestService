import React from 'react';
import {Card, CardActions, CardHeader, CardMedia, CardTitle, CardText} from 'material-ui';
import ReStock from "react-stockcharts";
import d3 from 'd3';

var parseDate = d3.time.format("%Y-%m-%d").parse

var { ChartCanvas, Chart, EventCapture } = ReStock;

var {BarSeries, AreaSeries,LineSeries,ScatterSeries, CircleMarker  } = ReStock.series;
var { XAxis, YAxis } = ReStock.axes;
var { fitWidth } = ReStock.helper;
var { MouseCoordinates, CurrentCoordinate } = ReStock.coordinates;
var { TooltipContainer, OHLCTooltip, MovingAverageTooltip,SingleValueTooltip } = ReStock.tooltip;
var { forceIndex,ema, sma, haikinAshi } = ReStock.indicator;

export default class Website extends React.Component {
  constructor (props) {
    super(props);
    this.state = {"responseTimeScale":[],"cpuMemoryScale":[]}
    this.restGetLatestData();
    var me = this;
    setInterval(function(){ me.restGetLatestData(); }, 5000);
  }

  ExtentsReponseTime(d) {
      return [d.responseTime,0];
  }

  ExtentsScale(d) {
      return [d.replicas + 1,0];
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
    while(data.CpuRam.length != counterCPUMemory) {
        var marge = Object.assign({}, data.CpuRam[counterCPUMemory], data.Scale[counterScale]);
        if(data.CpuRam[counterCPUMemory].JavaDate < data.Scale[counterScale].JavaDate) {
            marge.date = data.Scale[counterScale].JavaDate;

            counterScale++;
        } else {
            marge.date = data.CpuRam[counterCPUMemory].JavaDate;

            counterCPUMemory++;
        }

        this.state.cpuMemoryScale.push(marge);
    }

    if(counterCPUMemory != data.CpuRam.length) {
        while(data.CpuRam.length != counterCPUMemory) {
            var marge = Object.assign({}, data.ResponseTime[counterCPUMemory], data.Scale[data.Scale.length - 1]);
            marge.date = data.CpuRam[counterCPUMemory].JavaDate;
            this.state.cpuMemoryScale.push(marge);
            counterCPUMemory++;
        }

    }

    if(data.Scale.length != counterScale) {
        while(data.Scale.length != counterScale) {
            var marge = Object.assign({}, data.CpuRam[data.CpuRam.length - 1], data.Scale[counterScale]);
            marge.date = data.Scale[counterScale].JavaDate;
            this.state.cpuMemoryScale.push(marge);
             counterScale++;
        }

    }

    this.setState({"cpuMemoryScale":this.state.cpuMemoryScale});
  }

  responseTimeScale(data) {
    this.state.responseTimeScale = [];

    var counterScale = 0;
    var counterResponseTime = 0;
    while(data.ResponseTime.length != counterResponseTime && data.Scale.length != counterScale) {
        var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[counterScale]);
        if(data.ResponseTime[counterResponseTime].JavaDate < data.Scale[counterScale].JavaDate ) {
            if(counterResponseTime != 0){
                marge.date = data.Scale[counterScale].JavaDate;
                this.state.responseTimeScale.push(marge);
            }

            counterScale++;
        } else {
            marge.date = data.ResponseTime[counterResponseTime].JavaDate;
            this.state.responseTimeScale.push(marge);
            counterResponseTime++;
        }


    }

    if(counterResponseTime != data.ResponseTime.length) {
        while(data.ResponseTime.length != counterResponseTime) {
            var marge = Object.assign({}, data.ResponseTime[counterResponseTime], data.Scale[data.Scale.length - 1]);
            marge.date = data.ResponseTime[counterResponseTime].JavaDate;
            this.state.responseTimeScale.push(marge);
            counterResponseTime++;
        }

    }

    // if(data.Scale.length != counterScale) {
    //     while(data.Scale.length != counterScale) {
    //         var marge = Object.assign({}, data.ResponseTime[data.ResponseTime.length - 1], data.Scale[counterScale]);
    //         marge.date = data.Scale[counterScale].JavaDate;
    //         this.state.responseTimeScale.push(marge);
    //          counterScale++;
    //     }
    //
    // }

    this.setState({"responseTimeScale":this.state.responseTimeScale});
  }

  render () {
    var data = this.state.LatestData;

    //data.push({date: new Date(parseDate("2016-05-11").getTime()),scale: 1,cpu: 15, ram: 30});
    //data.push({date: new Date(parseDate("2016-05-12").getTime()),scale: 2,cpu: 5, ram: 30});
    //data.push({date: new Date(parseDate("2016-05-13").getTime()),scale: 2,cpu: 5, ram: 35});
    //data.push({date: new Date(parseDate("2016-05-14").getTime()),scale: 2,cpu: 5, ram: 30});
    //data.push({date: new Date(parseDate("2016-05-15").getTime()),scale: 1,cpu: 20, ram: 30});

    if(this.state.responseTimeScale.length == 0 || this.state.cpuMemoryScale.length == 0) {
        return (<div></div>);
    }

    return (
      <div >
        <Card>
         <CardHeader title={<h3> {"http://" + this.props.websiteData.map.HostName + ":" + this.props.websiteData.map.port + this.props.websiteData.map.path} </h3>} />
         <CardText>
           <h4>Reponse Time</h4>
             <ChartCanvas key={1} width={860} height={300}
               margin={{left: 50, right: 50, top:10, bottom: 30}}
               seriesName="MSFT"
               data={this.state.responseTimeScale} type="svg"
               xAccessor={d => d.date}  xScale={d3.time.scale()}>
               <Chart id={0} yExtents={this.ExtentsReponseTime} xAccessor={d => d.date}>
                 <XAxis axisAt="bottom" orient="bottom" ticks={6}/>
                 <YAxis axisAt="left" orient="left" />
                 <AreaSeries yAccessor={(d) => d.responseTime}/>
               </Chart>
               <Chart id={1} yExtents={this.ExtentsScale} height={150} origin={(w, h) => [0, h - 150]}>
                      <YAxis axisAt="right" orient="right" ticks={5} tickFormat={d3.format("s")}/>
                      <LineSeries yAccessor={d => d.replicas}/>
                      {
                        //<ScatterSeries yAccessor={d => d.replicas} marker={CircleMarker} markerProps={{ r: 3 }} />
                      }
                </Chart>
            </ChartCanvas>
            <h4>CPU RAM</h4>
           <ChartCanvas key={2} width={860} height={300}
             margin={{left: 50, right: 50, top:10, bottom: 30}}
             seriesName="MSFT"
             data={this.state.cpuMemoryScale} type="svg"
             xAccessor={d => d.date}  xScale={d3.time.scale()}>
           <Chart id={0} yExtents={[0,100]} xAccessor={d => d.date}>
             <XAxis axisAt="bottom" orient="bottom" ticks={6}/>
             <YAxis axisAt="left" orient="left" />

             <CurrentCoordinate id={1} yAccessor={(d) => d.Pcpu} fill="#9B0A47" />
  					 <CurrentCoordinate id={2} yAccessor={(d) => d.Pram} fill="#9B0A47" />

             <LineSeries yAccessor={(d) => d.Pcpu} stroke="#ff7f0e"/>
             <ScatterSeries yAccessor={(d) => d.Pcpu} marker={CircleMarker} markerProps={{ r: 3 }} />

             <LineSeries yAccessor={(d) => d.Pram} stroke="#2ca02c"/>
             <ScatterSeries yAccessor={(d) => d.Pram} marker={CircleMarker} markerProps={{ r: 3 }} />
           </Chart>
           <Chart id={1} yExtents={this.ExtentsScale} height={150} origin={(w, h) => [0, h - 150]}>
					        <YAxis axisAt="right" orient="right" ticks={5} tickFormat={d3.format("s")}/>
                  <AreaSeries yAccessor={d => d.replicas}/>
				    </Chart>
            <MouseCoordinates xDisplayFormat={d3.time.format("%Y-%m-%d")} />
				    <EventCapture mouseMove={true} zoom={true} pan={true} />
            <TooltipContainer>
              <SingleValueTooltip forChart={1}
                           yAccessor={d => d.Pram}
                           yLabel="ram"
                           yDisplayFormat={d3.format(".2f")}
                           valueStroke="#2ca02c"
                           /* labelStroke="#4682B4" - optional prop */
                           origin={[30, -85]}/>
              <SingleValueTooltip forChart={1}
              						yAccessor={d => d.Pcpu}
              						yLabel="cpu"
              						yDisplayFormat={d3.format(".2f")}
              						valueStroke="#ff7f0e"
              						/* labelStroke="#4682B4" - optional prop */
              						origin={[30, -100]}/>

            </TooltipContainer>
          </ChartCanvas>
         </CardText>
       </Card>
      </div>
    );
  }
}
