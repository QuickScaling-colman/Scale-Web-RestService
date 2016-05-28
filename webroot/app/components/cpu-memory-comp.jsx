import React from 'react';
import ReStock from "react-stockcharts";

var { ChartCanvas, Chart, EventCapture } = ReStock;

var {BarSeries, AreaSeries,LineSeries,ScatterSeries, CircleMarker  } = ReStock.series;
var { XAxis, YAxis } = ReStock.axes;
var { fitWidth } = ReStock.helper;
var { MouseCoordinates, CurrentCoordinate } = ReStock.coordinates;
var { TooltipContainer, OHLCTooltip, MovingAverageTooltip,SingleValueTooltip } = ReStock.tooltip;
var { forceIndex,ema, sma, haikinAshi } = ReStock.indicator;

export default class CpuMemoryComp extends React.Component {
  constructor (props) {
    super(props);
  }

  ExtentsScale(d) {
      return [d.replicas + 1,0];
  }

  render () {
      if(this.props.cpuMemoryScale.length == 0) {
          return (<div></div>);
      }

      return (
        <div>
         <h4>CPU RAM</h4>
         <ChartCanvas key={2} width={this.props.width - 50} height={300}
           margin={{left: 70, right: 50, top:10, bottom: 30}}
           seriesName="MSFT"
           data={this.props.cpuMemoryScale} type="svg"
           xAccessor={d => d.date}  xScale={d3.time.scale()}>
           <Chart id={0} yExtents={[0,100]} xAccessor={d => d.date}
             yMousePointerDisplayLocation="left" yMousePointerDisplayFormat={d3.format(".2f")}>
             <XAxis axisAt="bottom" orient="bottom" ticks={6}/>
             <YAxis axisAt="left" orient="left" percentScale={true} tickFormat={d3.format(".0%")}/>

             <CurrentCoordinate id={1} yAccessor={(d) => d.Pcpu} fill="#9B0A47" />
             <CurrentCoordinate id={2} yAccessor={(d) => d.Pram} fill="#9B0A47" />

             <AreaSeries yAccessor={(d) => d.Pcpu} fill={"#FF0000"} stroke="#ff7f0e"/>

             <AreaSeries yAccessor={(d) => d.Pram} fill={"#2ca02c"} stroke="#2ca02c"/>
           </Chart>
           <Chart id={1} yExtents={this.ExtentsScale} height={150} origin={(w, h) => [0, h - 150]}>
                <YAxis axisAt="right" orient="right" ticks={5} tickFormat={d3.format("s")}/>
                <LineSeries yAccessor={d => d.replicas}/>
           </Chart>
           <MouseCoordinates xDisplayFormat={d3.time.format("%d/%m/%y,%H:%M")} />
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
                        valueStroke="	#FF0000"
                        /* labelStroke="#4682B4" - optional prop */
                        origin={[30, -100]}/>

          </TooltipContainer>
        </ChartCanvas>
        </div>
      );
  }
}
