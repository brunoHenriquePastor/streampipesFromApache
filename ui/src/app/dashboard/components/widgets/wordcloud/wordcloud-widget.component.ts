/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Component, OnDestroy, OnInit} from "@angular/core";
import {RxStompService} from "@stomp/ng2-stompjs";
import {BaseStreamPipesWidget} from "../base/base-widget";
import {StaticPropertyExtractor} from "../../../sdk/extractor/static-property-extractor";
import {ResizeService} from "../../../services/resize.service";
import {DashboardService} from "../../../services/dashboard.service";
import {EventPropertyList} from "../../../../core-model/gen/streampipes-model";
import {WordCloudConfig} from "./wordcloud-config";

import { EChartsOption } from 'echarts';
import 'echarts-wordcloud';
import {ECharts} from "echarts/core";



@Component({
  selector: 'wordcloud-widget',
  templateUrl: './wordcloud-widget.component.html',
  styleUrls: ['./wordcloud-widget.component.scss']
})
export class WordcloudWidgetComponent extends BaseStreamPipesWidget implements OnInit, OnDestroy {

  countProperty: string;
  nameProperty: string;
  windowSize: number;
  eventProperty: EventPropertyList;

  words: Array<any> = new Array<any>();

  currentWidth: number;
  currentHeight: number;

  configReady: boolean = false;

  eChartsInstance: ECharts;
  dynamicData: EChartsOption;
  // @ts-ignore
  chartOption: any = {
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '100%',
      height: '100%',
      right: null,
      bottom: null,
      sizeRange: [8, 50],
      rotationRange: [-90, 90],
      rotationStep: 45,
      gridSize: 8,
      drawOutOfBound: false,
      layoutAnimation: true,

      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return 'rgb(' + [
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160)
          ].join(',') + ')';
        }
      },
      emphasis: {
        focus: 'self',

        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: []
    }]
  };

  constructor(rxStompService: RxStompService, dashboardService: DashboardService, resizeService: ResizeService) {
    super(rxStompService, dashboardService, resizeService, false);
  }

  protected extractConfig(extractor: StaticPropertyExtractor) {
    this.countProperty = extractor.mappingPropertyValue(WordCloudConfig.COUNT_PROPERTY_KEY);
    this.nameProperty = extractor.mappingPropertyValue(WordCloudConfig.NAME_PROPERTY_KEY);
    this.windowSize = extractor.integerParameter(WordCloudConfig.WINDOW_SIZE_KEY);
  }

  protected onEvent(event: any) {
    let value = event[this.countProperty];
    let name = event[this.nameProperty];
    this.dynamicData = this.chartOption;
    if (this.dynamicData.series[0].data.some(d => d.name == name)) {
      this.dynamicData.series[0].data.find(d => d.name == name).value = value;
    } else {
      this.dynamicData.series[0].data.push({name: name, value: value});
    }
    if (this.dynamicData.series[0].data.length > this.windowSize) {
      this.dynamicData.series[0].data.shift();
    }
    this.eChartsInstance.setOption(this.dynamicData);
  }

  protected onSizeChanged(width: number, height: number) {
    this.currentWidth = width;
    this.currentHeight = height;
    this.configReady = true;
    this.applySize(width, height);
  }

  onChartInit(ec) {
    this.eChartsInstance = ec;
    this.applySize(this.currentWidth, this.currentHeight);
  }

  applySize(width: number, height: number) {
    if (this.eChartsInstance) {
      this.eChartsInstance.resize({width: width, height: height});
    }
  }

}
