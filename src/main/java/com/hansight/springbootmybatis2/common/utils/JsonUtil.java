package com.hansight.springbootmybatis2.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Arvin
 *
 */
public class JsonUtil {
	
	private static ObjectMapper mapper = new ObjectMapper();
	static{
		//属性为NULL 不序列化
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	}

	public static String obj2Json(Object obj) throws IOException {
		return mapper.writeValueAsString(obj);
	}
	
	public static byte[] obj2JsonBytes(Object obj) throws IOException {
		return mapper.writeValueAsBytes(obj);
	}
	
	public static <T> T json2Obj(String json, Class<? extends T> clazz) throws IOException {
		return mapper.readValue(json, clazz);
	}
	
	public static <T> T json2Obj(byte[] json, Class<? extends T> clazz) throws IOException {
		return mapper.readValue(json, clazz);
	}

    public static <T> T json2Obj(String json, TypeReference typeReference) throws IOException {
        return mapper.readValue(json, typeReference);
    }


    public static void main(String[] args) throws IOException {
        Map m = new HashMap();
        m.put("key", "i am in List");

        List l = new ArrayList();
        l.add(m);

        Map m2 = new HashMap();
        m2.put("set a list", l);

        String s1= obj2Json(m2);

        Map<String, List<Map>> m3 = json2Obj(s1, new TypeReference<Map<String, List<Map>>>() {});
        System.out.println(m3.get("set a list"));
        System.out.println(m3.get("set a list").get(0));


    }
}
