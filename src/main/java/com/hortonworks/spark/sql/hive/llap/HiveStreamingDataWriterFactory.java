package com.hortonworks.spark.sql.hive.llap;

import com.hortonworks.spark.hive.utils.HiveIsolatedClassLoader;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.writer.DataWriter;
import org.apache.spark.sql.sources.v2.writer.DataWriterFactory;
import org.apache.spark.sql.types.StructType;

import java.util.List;

public class HiveStreamingDataWriterFactory implements DataWriterFactory<InternalRow> {

  private String jobId;
  private StructType schema;
  private long commitIntervalRows;
  private String db;
  private String table;
  private List<String> partition;
  private String metastoreUri;
  private String metastoreKrbPrincipal;

  public HiveStreamingDataWriterFactory(String jobId, StructType schema, long commitIntervalRows, String db,
    String table, List<String> partition, final String metastoreUri, final String metastoreKrbPrincipal) {
    this.jobId = jobId;
    this.schema = schema;
    this.db = db;
    this.table = table;
    this.partition = partition;
    this.commitIntervalRows = commitIntervalRows;
    this.metastoreUri = metastoreUri;
    this.metastoreKrbPrincipal = metastoreKrbPrincipal;
  }

  @Override
  public DataWriter<InternalRow> createDataWriter(int partitionId, long taskId, long epochId) {
    ClassLoader restoredClassloader = Thread.currentThread().getContextClassLoader();
    ClassLoader isolatedClassloader = HiveIsolatedClassLoader.isolatedClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(isolatedClassloader);
      return new HiveStreamingDataWriter(jobId, schema, commitIntervalRows, partitionId, taskId, epochId, db,
        table, partition, metastoreUri, metastoreKrbPrincipal);
    } finally {
      Thread.currentThread().setContextClassLoader(restoredClassloader);
    }
  }
}

