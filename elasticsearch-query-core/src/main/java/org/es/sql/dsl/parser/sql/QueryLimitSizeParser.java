package org.es.sql.dsl.parser.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import org.es.sql.druid.ElasticSqlSelectQueryBlock;
import org.es.sql.dsl.bean.ElasticDslContext;
import org.es.sql.dsl.exception.ElasticSql2DslException;
import org.es.sql.dsl.helper.ElasticSqlArgTransferHelper;
import org.es.sql.dsl.listener.ParseActionListener;

public class QueryLimitSizeParser implements QueryParser {

    private ParseActionListener parseActionListener;

    public QueryLimitSizeParser(ParseActionListener parseActionListener) {
        this.parseActionListener = parseActionListener;
    }

    @Override
    public void parse(ElasticDslContext dslContext) {
        ElasticSqlSelectQueryBlock queryBlock = (ElasticSqlSelectQueryBlock) dslContext.getQueryExpr().getSubQuery().getQuery();
        if (queryBlock.getLimit() != null) {
            Integer from = parseLimitInteger(queryBlock.getLimit().getOffset(), dslContext.getSqlArgs());
            dslContext.getParseResult().setFrom(from);

            Integer size = parseLimitInteger(queryBlock.getLimit().getRowCount(), dslContext.getSqlArgs());
            dslContext.getParseResult().setSize(size);
        }
        else {
            dslContext.getParseResult().setFrom(0);
            dslContext.getParseResult().setSize(15);
        }
    }

    public Integer parseLimitInteger(SQLExpr limitInt, Object[] args) {
        if (limitInt instanceof SQLIntegerExpr) {
            return ((SQLIntegerExpr) limitInt).getNumber().intValue();
        }
        else if (limitInt instanceof SQLVariantRefExpr) {
            SQLVariantRefExpr varLimitExpr = (SQLVariantRefExpr) limitInt;
            Object targetVal = ElasticSqlArgTransferHelper.transferSqlArg(varLimitExpr, args);
            if (!(targetVal instanceof Integer)) {
                throw new ElasticSql2DslException("[syntax error] Sql limit expr should be a non-negative number");
            }
            return Integer.valueOf(targetVal.toString());
        }
        else {
            throw new ElasticSql2DslException("[syntax error] Sql limit expr should be a non-negative number");
        }
    }
}
