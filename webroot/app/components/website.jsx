import React from 'react';
import {Card, CardActions, CardHeader, CardMedia, CardTitle, CardText} from 'material-ui';
import ReStock from "react-stockcharts";
import d3 from 'd3';

var parseDate = d3.time.format("%Y-%m-%d").parse

var { ChartCanvas, Chart } = ReStock;

var { AreaSeries,LineSeries,ScatterSeries, CircleMarker } = ReStock.series;
var { XAxis, YAxis } = ReStock.axes;
var { fitWidth } = ReStock.helper;

export default class Website extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    var data = [];

    data.push({date: new Date(parseDate("2016-05-11").getTime()),count: 20.0,cpu: 15});
    data.push({date: new Date(parseDate("2016-05-12").getTime()),count: 30.0,cpu: 10});
    data.push({date: new Date(parseDate("2016-05-13").getTime()),count: 25.0,cpu: 12});
    data.push({date: new Date(parseDate("2016-05-14").getTime()),count: 10.0,cpu: 11});
    data.push({date: new Date(parseDate("2016-05-15").getTime()),count: 60.0,cpu: 15});



    return (
      <div >
        <Card>
         <CardHeader title={<h3> {this.props.websiteData.map.URL} </h3>} />
         <CardText>
           <ChartCanvas width={580} height={400}
             margin={{left: 50, right: 50, top:10, bottom: 30}}
             seriesName="MSFT"
             data={data} type="svg"
             xAccessor={d => d.date}  xScale={d3.time.scale()}>
           <Chart id={0} yExtents={d => d.count} xAccessor={d => d.date}>
             <XAxis axisAt="bottom" orient="bottom" ticks={6}/>
             <YAxis axisAt="left" orient="left" />
             <AreaSeries yAccessor={(d) => d.count}/>

           </Chart>
           <Chart id={2} yExtents={d => d.cpu} height={150} origin={(w, h) => [0, h - 150]}>
					        <YAxis axisAt="right" orient="right" ticks={5} tickFormat={d3.format("s")}/>
                  <LineSeries yAccessor={d => d.cpu}/>
                  <ScatterSeries yAccessor={d => d.cpu} marker={CircleMarker} markerProps={{ r: 3 }} />
				    </Chart>
          </ChartCanvas>
         </CardText>
       </Card>
      </div>
    );
  }
}
