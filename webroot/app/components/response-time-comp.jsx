import React from 'react';
import ReStock from "react-stockcharts";

var { ChartCanvas, Chart, EventCapture } = ReStock;
var {AreaSeries,LineSeries} = ReStock.series;
var { XAxis, YAxis } = ReStock.axes;
var { MouseCoordinates, CurrentCoordinate } = ReStock.coordinates;

export default class ResponseTimeComp extends React.Component {
  constructor (props) {
    super(props);
  }

  ExtentsReponseTime(d) {
      return [d.responseTime,0];
  }

  ExtentsScale(d) {
      return [d.replicas + 1,0];
  }

  render () {
    if(this.props.responseTimeScale.length == 0) {
        return (<div></div>);
    }
      return (
        <div>
          <h4>Reponse Time</h4>
          <ChartCanvas key={1} width={this.props.width - 50} height={this.props.height}
            margin={{left: 70, right: 50, top:10, bottom: 30}}
            seriesName="MSFT"
            data={this.props.responseTimeScale} type="hybrid"
            xAccessor={d => d.date}
            xScale={d3.time.scale()}>
            <Chart id={0} yExtents={this.ExtentsReponseTime} xAccessor={d => d.date}
              yMousePointerDisplayLocation="left" yMousePointerDisplayFormat={d3.format(".2f")}>
              <XAxis axisAt="bottom" orient="bottom" ticks={10}/>
              <YAxis axisAt="left" orient="left" />
              <CurrentCoordinate id={1} yAccessor={(d) => d.responseTime} fill="#9B0A47" />
              <AreaSeries yAccessor={(d) => d.responseTime} fill="#9B0A47"/>
            </Chart>
            <Chart id={1} yExtents={this.ExtentsScale} height={150} origin={(w, h) => [0, h - 150]}>
                   <YAxis axisAt="right" orient="right" ticks={5} tickFormat={d3.format("s")}/>
                   <LineSeries yAccessor={d => d.replicas}/>
                   {
                     //<ScatterSeries yAccessor={d => d.replicas} marker={CircleMarker} markerProps={{ r: 3 }} />
                   }
             </Chart>
             <MouseCoordinates xDisplayFormat={d3.time.format("%d/%m/%y,%H:%M")} />
             <EventCapture mouseMove={true} zoom={true} pan={true} />
         </ChartCanvas>
       </div>
      );
  }
}
