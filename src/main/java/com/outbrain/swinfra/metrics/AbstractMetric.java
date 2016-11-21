package com.outbrain.swinfra.metrics;

import com.codahale.metrics.Metric;
import com.outbrain.swinfra.metrics.children.ChildMetricRepo;
import com.outbrain.swinfra.metrics.children.MetricData;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.outbrain.swinfra.metrics.StringUtils.isNotBlank;

abstract class AbstractMetric<T extends Metric> {

  private final String name;
  private final String help;
  private final List<String> labelNames;
  private ChildMetricRepo<T> childMetricRepo;

  AbstractMetric(final String name,
                 final String help,
                 final String[] labelNames) {
    this.name = name;
    this.help = help;
    this.labelNames = Arrays.asList(labelNames);
  }

  abstract ChildMetricRepo<T> createChildMetricRepo();

  abstract Collector.Type getType();

  abstract MetricFamilySamples toMetricFamilySamples(final MetricData<T> metricData);

  String getName() {
    return name;
  }

  String getHelp() {
    return help;
  }

  List<String> getLabelNames() {
    return labelNames;
  }

  void initChildMetricRepo() {
    this.childMetricRepo = createChildMetricRepo();
  }

  void validateLabelValues(final String... labelValues) {
    if (labelNames.size() > 0) {
      checkArgument(labelNames.size() == labelValues.length, "A label value must be supplied for each label name");
    }

    for (final String labelName : labelNames) {
      checkArgument(isNotBlank(labelName), "Label names must contain text");
    }
  }

  T metricForLabels(final String... labelValues) {
    return childMetricRepo.metricForLabels(labelValues).getMetric();
  }

  List<MetricFamilySamples> getSamples() {
    return childMetricRepo.all()
                          .stream()
                          .map(this::toMetricFamilySamples)
                          .collect(Collectors.toList());
  }

}
