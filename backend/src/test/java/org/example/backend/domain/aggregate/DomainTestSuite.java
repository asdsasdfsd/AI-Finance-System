// backend/src/test/java/org/example/backend/domain/aggregate/DomainTestSuite.java
package org.example.backend.domain.aggregate;

import org.example.backend.domain.aggregate.company.CompanyAggregateTest;
import org.example.backend.domain.aggregate.user.UserAggregateTest;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateTest;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregateTest;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregateTest;
import org.example.backend.domain.aggregate.report.ReportAggregateTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Domain Aggregate Test Suite
 * 
 * 运行所有聚合根测试的测试套件
 */
@Suite
@SelectClasses({
    CompanyAggregateTest.class,
    UserAggregateTest.class,
    TransactionAggregateTest.class,
    JournalEntryAggregateTest.class,
    FixedAssetAggregateTest.class,
    ReportAggregateTest.class
})
public class DomainTestSuite {
    // 测试套件类，用于批量运行所有聚合根测试
}