package org.hzh.mybatis.analyzer;

import java.util.Set;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hzh.mybatis.listener.ParamListener;
import org.hzh.mybatis.listener.ParamListener.ParamInfo;
import org.hzh.mybatis.listener.SqlProcessorListener;
import org.hzh.mybatis.parser.MySqlParser;
import org.hzh.mybatis.utils.DebugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseResult {
	private static final Logger logger = LoggerFactory.getLogger(ParseResult.class);
	
	String originalSql;
	ParseTree tree;
	TokenStream tokens;
	MySqlParser parser;
	// to do: add dynamic property for performance

	// parameter appear in sql
	private Set<ParamInfo> sqlParamInfos;

	public MySqlParser getParser() {
		return parser;
	}

	public ParseTree getTree() {
		return tree;
	}

	public Set<ParamInfo> getSqlParamInfos() {
		if (sqlParamInfos == null) {
			synchronized (this) {
				if (sqlParamInfos == null) {
					ParamListener listener = new ParamListener();
					ParseTreeWalker walker = new ParseTreeWalker();
					walker.walk(listener, tree);

					sqlParamInfos = listener.getParamInfos();
				}
			}
		}

		return sqlParamInfos;
	}

	public ApplyParamResult apply(Object param) {
//		TokenStreamRewriter rewriter = new TokenStreamRewriter(tokens);
		SqlProcessorListener listener = new SqlProcessorListener(tokens, param);
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, tree);

		String executaleSql = listener.getText();
		ApplyParamResult applyParamResult = new ApplyParamResult();
		applyParamResult.executaleSql = executaleSql;
		applyParamResult.paramList = listener.getParamList();
		logger.debug(applyParamResult.toString());

		assert executaleSql.indexOf('#') == -1
				&& DebugUtils.countForChar(executaleSql, '?') == listener.getParamList().size();

		return applyParamResult;
	}
}
