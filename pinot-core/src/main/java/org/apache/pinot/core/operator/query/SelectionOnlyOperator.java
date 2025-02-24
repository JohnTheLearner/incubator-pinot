/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.operator.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.common.request.Selection;
import org.apache.pinot.common.request.transform.TransformExpressionTree;
import org.apache.pinot.common.utils.DataSchema;
import org.apache.pinot.core.common.BlockValSet;
import org.apache.pinot.core.common.RowBasedBlockValueFetcher;
import org.apache.pinot.core.indexsegment.IndexSegment;
import org.apache.pinot.core.operator.BaseOperator;
import org.apache.pinot.core.operator.ExecutionStatistics;
import org.apache.pinot.core.operator.blocks.IntermediateResultsBlock;
import org.apache.pinot.core.operator.blocks.TransformBlock;
import org.apache.pinot.core.operator.transform.TransformOperator;
import org.apache.pinot.core.operator.transform.TransformResultMetadata;
import org.apache.pinot.core.query.selection.SelectionOperatorUtils;


public class SelectionOnlyOperator extends BaseOperator<IntermediateResultsBlock> {
  private static final String OPERATOR_NAME = "SelectionOnlyOperator";

  private final IndexSegment _indexSegment;
  private final TransformOperator _transformOperator;
  private final List<TransformExpressionTree> _expressions;
  private final BlockValSet[] _blockValSets;
  private final DataSchema _dataSchema;
  private final int _numRowsToKeep;
  private final List<Serializable[]> _rows;

  private ExecutionStatistics _executionStatistics;

  public SelectionOnlyOperator(IndexSegment indexSegment, Selection selection, TransformOperator transformOperator) {
    _indexSegment = indexSegment;
    _transformOperator = transformOperator;
    _expressions = SelectionOperatorUtils.extractExpressions(selection.getSelectionColumns(), indexSegment, null);

    int numExpressions = _expressions.size();
    _blockValSets = new BlockValSet[numExpressions];
    String[] columnNames = new String[numExpressions];
    DataSchema.ColumnDataType[] columnDataTypes = new DataSchema.ColumnDataType[numExpressions];
    for (int i = 0; i < numExpressions; i++) {
      TransformExpressionTree expression = _expressions.get(i);
      TransformResultMetadata expressionMetadata = _transformOperator.getResultMetadata(expression);
      columnNames[i] = expression.toString();
      columnDataTypes[i] =
          DataSchema.ColumnDataType.fromDataType(expressionMetadata.getDataType(), expressionMetadata.isSingleValue());
    }
    _dataSchema = new DataSchema(columnNames, columnDataTypes);

    _numRowsToKeep = selection.getSize();
    _rows = new ArrayList<>(Math.min(_numRowsToKeep, SelectionOperatorUtils.MAX_ROW_HOLDER_INITIAL_CAPACITY));
  }

  @Override
  protected IntermediateResultsBlock getNextBlock() {
    int numDocsScanned = 0;

    TransformBlock transformBlock;
    while ((transformBlock = _transformOperator.nextBlock()) != null) {
      int numExpressions = _expressions.size();
      for (int i = 0; i < numExpressions; i++) {
        _blockValSets[i] = transformBlock.getBlockValueSet(_expressions.get(i));
      }
      RowBasedBlockValueFetcher blockValueFetcher = new RowBasedBlockValueFetcher(_blockValSets);

      int numDocsToAdd = Math.min(_numRowsToKeep - _rows.size(), transformBlock.getNumDocs());
      numDocsScanned += numDocsToAdd;
      for (int i = 0; i < numDocsToAdd; i++) {
        _rows.add(blockValueFetcher.getRow(i));
      }
      if (_rows.size() == _numRowsToKeep) {
        break;
      }
    }

    // Create execution statistics.
    long numEntriesScannedInFilter = _transformOperator.getExecutionStatistics().getNumEntriesScannedInFilter();
    long numEntriesScannedPostFilter = numDocsScanned * _transformOperator.getNumColumnsProjected();
    long numTotalRawDocs = _indexSegment.getSegmentMetadata().getTotalRawDocs();
    _executionStatistics =
        new ExecutionStatistics(numDocsScanned, numEntriesScannedInFilter, numEntriesScannedPostFilter,
            numTotalRawDocs);

    return new IntermediateResultsBlock(_dataSchema, _rows);
  }

  @Override
  public String getOperatorName() {
    return OPERATOR_NAME;
  }

  @Override
  public ExecutionStatistics getExecutionStatistics() {
    return _executionStatistics;
  }
}
