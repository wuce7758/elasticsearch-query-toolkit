package org.es.sql.dsl.helper;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import org.es.sql.dsl.exception.ElasticSql2DslException;

import java.util.List;

public class ElasticSqlArgTransferHelper {

    public static Object[] transferSqlArgs(List<SQLExpr> exprList, Object[] sqlArgs) {
        Object[] values = new Object[exprList.size()];
        for (int idx = 0; idx < exprList.size(); idx++) {
            values[idx] = transferSqlArg(exprList.get(idx), sqlArgs, true);
        }
        return values;
    }

    public static Object transferSqlArg(SQLExpr expr, Object[] sqlArgs) {
        return transferSqlArg(expr, sqlArgs, true);
    }

    public static Object transferSqlArg(SQLExpr expr, Object[] sqlArgs, boolean recognizeDateArg) {
        if (expr instanceof SQLVariantRefExpr) {
            SQLVariantRefExpr varRefExpr = (SQLVariantRefExpr) expr;
            if (sqlArgs == null || sqlArgs.length == 0) {
                throw new ElasticSql2DslException("[syntax error] Sql args cannot be blank");
            }
            if (varRefExpr.getIndex() >= sqlArgs.length) {
                throw new ElasticSql2DslException("[syntax error] Sql args out of index: " + varRefExpr.getIndex());
            }
            //parse date
            if (recognizeDateArg && ElasticSqlDateParseHelper.isDateArgObjectValue(sqlArgs[varRefExpr.getIndex()])) {
                return ElasticSqlDateParseHelper.formatDefaultEsDateObjectValue(sqlArgs[varRefExpr.getIndex()]);
            }
            return sqlArgs[varRefExpr.getIndex()];
        }

        //numbers
        if (expr instanceof SQLIntegerExpr) {
            return ((SQLIntegerExpr) expr).getNumber().longValue();
        }
        if (expr instanceof SQLNumberExpr) {
            return ((SQLNumberExpr) expr).getNumber().doubleValue();
        }

        //string
        if (expr instanceof SQLCharExpr) {
            Object textObject = ((SQLCharExpr) expr).getValue();
            //parse date
            if (recognizeDateArg && (textObject instanceof String) && ElasticSqlDateParseHelper.isDateArgStringValue((String) textObject)) {
                return ElasticSqlDateParseHelper.formatDefaultEsDateStringValue((String) textObject);
            }
            return textObject;
        }

        //method call
        if (expr instanceof SQLMethodInvokeExpr) {
            SQLMethodInvokeExpr methodExpr = (SQLMethodInvokeExpr) expr;

            //parse date method
            if (ElasticSqlDateParseHelper.isDateMethod(methodExpr)) {
                ElasticSqlMethodInvokeHelper.checkDateMethod(methodExpr);
                String patternArg = (String) ElasticSqlArgTransferHelper.transferSqlArg(methodExpr.getParameters().get(0), sqlArgs, false);
                String timeValArg = (String) ElasticSqlArgTransferHelper.transferSqlArg(methodExpr.getParameters().get(1), sqlArgs, false);
                return ElasticSqlDateParseHelper.formatDefaultEsDate(patternArg, timeValArg);
            }
        }
        throw new ElasticSql2DslException("[syntax error] Can not support arg type: " + expr.toString());
    }
}
