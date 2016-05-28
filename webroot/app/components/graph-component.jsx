import React from 'react';

import {Xgrid,Ygrid,Legend,Xaxis,Yaxis, scale, xDomainCount, yDomainCount} from 'react-d3-core';
import {Area,Line,Chart,series} from 'react-d3-shape';
import {Voronoi,Tooltip} from 'react-d3-tooltip';
import Focus from './focus.jsx';

import {AreaStackZoom} from 'react-d3-zoom';

import {BrushSet,Brush,BrushFocus} from 'react-d3-brush';

export default class GraphComponent extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      focusX: -100,
      focusY: -200,
      xTooltip: -100,
      yTooltip: -100,
      contentTooltip: null
    };

    this.state.showScatter = false;
    this.state.x = this.chartGetDate;
    this.state.y = this.chartGety;
    this.state.xScale = "time";
    this.state.yScale = 'linear';
    this.state.yTickOrient = 'right';
    this.state.chartSeries = this.props.chartSeries;
    this.state.data = this.props.Data;
    this.state.title = "test";
    this.state.width = this.props.width - 60;
    this.state.height = this.props.height;
    this.state.margins = {left: 60, right: 20, top:30, bottom: 30};
    this.state.brushHeight = 100;
    const brushMargins = this.props.brushMargins || {top: 30, right: 0, bottom: 30, left: this.state.margins.left}
    const yBrushRange = this.props.yBrushRange || [this.state.brushHeight - brushMargins.top - brushMargins.bottom, 0]
    const xDomain = this.mkXDomain();
    const yDomain = this.mkYDomain(true);

    this.state.xRange = this.props.xRange || [0, this.state.width - this.state.margins.left - this.state.margins.right];
    this.state.yRange =  this.props.yRange || [this.state.height - this.state.margins.top - this.state.margins.bottom, 0];
    this.state.xRangeRoundBands = this.props.xRangeRoundBands || {interval: [0, this.state.width - this.state.margins.left - this.state.margins.right], padding: .1};
    this.state.brushMargins = brushMargins;
    this.state.yBrushRange = yBrushRange;
    this.state.xDomainSet = xDomain;
    this.state.yDomainSet = yDomain;
  }
  chartGetDate(d) {
    return d.date;
  }

  chartGety(d) {
    return +d;
  }

  chartGetValue(d) {
    return d;
  }

  chartMoushPointer(y) {
    return y.toFixed(2);
  }

  voronoiMouseOut(d, i) {
    this.setState({
      focusX: -100,
      focusY: -100,
      xTooltip: null,
      yTooltip: null,
      contentTooltip: null
    })
  }

  voronoiMouseOver(e, d, xScaleSet, yScaleSet, stack) {
    var newY = stack? yScaleSet(d.y + d.y0): yScaleSet(d.y);
    let DateTime = d.x.getDate() + "/" + d.x.getMonth() + "/" + d.x.getFullYear() + " , " + d.x.getHours() + ":" + d.x.getMinutes() + ":" + d.x.getSeconds();
    const contentTooltip = {Date: DateTime, value: d.y.toString()};

    this.setState({
      focusX: xScaleSet(d.x),
      focusY: newY,
      xTooltip: e.clientX,
      yTooltip: e.clientY,
      contentTooltip: contentTooltip
    })
  }

  mkXDomain() {
    return this.setXDomain = xDomainCount(this.state);
  }

  mkYDomain(stack) {
    return this.setYDomain = yDomainCount(this.state, stack);
  }

  setDomain(axis, val) {
    const {
      xScale,
      xRange,
      xDomain,
      xRangeRoundBands,
      yScale,
      yRange,
      yDomain,
      yRangeRoundBands
    } = this.state;

    if(axis === 'x'){

      var xScaleChange = {
        scale: xScale,
        range: xRange,
        domain: val,
        rangeRoundBands: xRangeRoundBands
      }

      // set x scale
      this.setState({
        xDomainSet: val,
        xScaleSet: scale(xScaleChange)
      })
    }else if(axis === 'y'){

      var yScaleChange = {
        scale: yScale,
        range: yRange,
        domain: val,
        rangeRoundBands: yRangeRoundBands
      }

      // set y scale
      this.setState({
        yDomainSet: val,
        yScaleSet: scale(yScaleChange)
      })
    }
  }

  render () {
    let chartComp = {};
    let voronoi = "";
    let brush = "";
    let brushFocus = "";

    switch (this.props.GraphType) {
      case "Area":
        chartComp = <Area key={1} {...this.state} />

        let chartSeriesData = series(this.state)

        // brush = <Brush xDomain= {this.setXDomain}
        //                    yDomain= {this.setYDomain}
        //                    {...this.state}
        //                    brushType="area_stack"
        //                    chartSeriesData={chartSeriesData}
        //                    setDomain={this.setDomain.bind(this)} />
        //
        // brushFocus = <BrushFocus {...this.state}/>

        break;
      case "AreaStep":
        chartComp = <Area {...this.state} interpolate={"step-after"} />
        break;
      default:

    }

    voronoi = <Voronoi key={2}
            {...this.state}
            onMouseOver= {this.voronoiMouseOver.bind(this)}
            onMouseOut= {this.voronoiMouseOut.bind(this)}
            />


    let ChartComp = <div >
                        <Tooltip {...this.state} />
                        <Chart {...this.state} xDomain={this.state.xDomainSet}>
                          {chartComp}
                          <Xgrid xDomain={this.state.xDomainSet}/>
                          <Ygrid />
                          <Xaxis  xDomain={this.state.xDomainSet}/>
                          <Yaxis />
                          {voronoi}
                          <Focus {...this.props} {...this.state}/>
                          {brushFocus}
                        </Chart>
                        {brush}
                      </div>
    return (
      <div style={{width:this.state.width}}>
        <h3>{this.props.title}</h3>
        {ChartComp}
      </div>
    )
  }
}
