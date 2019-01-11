package com.hansight.springbootmybatis2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.springbootmybatis2.common.elasticsearch.util.ESSearchUtil;
import com.hansight.springbootmybatis2.common.vul.factory.VulRestClient;
import com.hansight.springbootmybatis2.demo.config.EsConfig;
import com.hansight.springbootmybatis2.demo.config.VulScanConfig;
import com.hansight.springbootmybatis2.demo.dao.UserMapper;
import com.hansight.springbootmybatis2.demo.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.hansight.springbootmybatis2.StatusUtils.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootMybatis2ApplicationTests {

	@Test
	public void contextLoads() {
	}


	@Autowired
	private EsConfig esConfig;

	@Autowired
	private UserMapper userMapper;

	@Test
	public void test(){
		esConfig.getClusterName();
		List<User> lists = userMapper.findByUsername("admin");
		System.out.println(lists);
	}

	@Autowired
	ESSearchUtil esSearchUtil;
	@Test
	public void test2() throws IOException {
		BoolQueryBuilder idFilter = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termsQuery("confirm_status", Arrays.asList(UNKNOWN, CONFIRMED_SUCCESS, CONFIRMED_FAILED)))//攻击确认: 未知、已确认攻击成功、已确认攻击未成功、误报
				.filter(QueryBuilders.termQuery("handle_status", UNPROCESS));
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(idFilter);
		SearchResponse searchResponse = esSearchUtil.searchResponseFixedIndex("incident", 5, queryBuilder);
		searchResponse.getHits();

	}

	@Autowired
	VulRestClient vulRestClient;
	@Test
	public void test3() throws IOException {
		RestClient restClient = vulRestClient.getLowRestClient();
		Header userID = new BasicHeader("TOKEN", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHQiOjkyMjMzNzIwMzY4NTQ3NzU4MDcsInVpZCI6IkZJS1ZJWlMzMDAwNyIsImN0IjoxNTQ2ODg2MDY4NDk0LCJsbiI6ImFkbWluIiwic2FkbSI6dHJ1ZSwiY2lwIjoiMTI3LjAuMC4xIiwicm4iOiLotoXnuqfnrqHnkIblkZgiLCJpYXQiOjE1NDY4ODYwNjg0OTR9.JxhUP3WprZZehF7OW7FfD7YrfsyBZdxqHDagPZ2J75Q");
		Response response = restClient.performRequest(HttpPost.METHOD_NAME,"/__api/security/knowledgeType/query", userID);
		System.out.println(EntityUtils.toString(response.getEntity(), Consts.UTF_8));

	}

	public BoolQueryBuilder builderTime(long startTime, long endTime){
		return QueryBuilders.boolQuery()
				.should(QueryBuilders.rangeQuery("start_time").gte(startTime).lte(endTime))
				.should(QueryBuilders.rangeQuery("update_time").gte(startTime).lte(endTime))
				.should(QueryBuilders.boolQuery()
						.filter(QueryBuilders.rangeQuery("start_time").lte(startTime))
						.filter(QueryBuilders.rangeQuery("update_time").gte(endTime)));
	}

}

