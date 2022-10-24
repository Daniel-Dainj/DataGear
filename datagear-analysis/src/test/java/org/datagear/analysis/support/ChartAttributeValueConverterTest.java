/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartAttribute;
import org.junit.Test;

/**
 * {@linkplain ChartAttributeValueConverter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ChartAttributeValueConverterTest
{
	public ChartAttributeValueConverterTest()
	{
		super();
	}

	@Test
	public void convertTest()
	{
		List<ChartAttribute> chartAttributes = new ArrayList<>();
		chartAttributes.add(new ChartAttribute("name", ChartAttribute.DataType.STRING, true, false));
		chartAttributes.add(new ChartAttribute("size", ChartAttribute.DataType.NUMBER, true, false));
		chartAttributes.add(new ChartAttribute("enable", ChartAttribute.DataType.BOOLEAN, true, false));
		
		ChartAttributeValueConverter converter = new ChartAttributeValueConverter();

		{
			Map<String, Object> actual = converter.convert(null, chartAttributes);
			assertNull(actual);
		}
		
		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", "aaa");
			values.put("size", "3");
			values.put("enable", "true");
			values.put("other0", 5);
			values.put("other1", "false");

			Map<String, Object> actual = converter.convert(values, chartAttributes);

			assertEquals("aaa", actual.get("name"));
			assertEquals(3, ((Integer) actual.get("size")).intValue());
			assertEquals(true, ((Boolean) actual.get("enable")).booleanValue());
			assertEquals(5, ((Integer) actual.get("other0")).intValue());
			assertEquals("false", actual.get("other1"));
		}

		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", new String[] { "aaa", "bbb" });
			values.put("size", new String[] { "3", "4" });
			values.put("enable", new String[] { "true", "false" });
			values.put("other0", new int[] { 5, 6 });
			values.put("other1", new boolean[] { false, false });

			Map<String, Object> actual = converter.convert(values, chartAttributes);
			
			Object[] actualName = (Object[])actual.get("name");
			Object[] actualSize = (Object[])actual.get("size");
			Object[] actualEnable = (Object[])actual.get("enable");
			int[] actualOther0 = (int[])actual.get("other0");
			boolean[] actualOther1 = (boolean[])actual.get("other1");

			assertEquals(2, actualName.length);
			assertEquals("aaa", actualName[0]);
			assertEquals("bbb", actualName[1]);

			assertEquals(2, actualSize.length);
			assertEquals(3, ((Integer) actualSize[0]).intValue());
			assertEquals(4, ((Integer) actualSize[1]).intValue());

			assertEquals(2, actualEnable.length);
			assertEquals(true, ((Boolean) actualEnable[0]).booleanValue());
			assertEquals(false, ((Boolean) actualEnable[1]).booleanValue());

			assertEquals(2, actualOther0.length);
			assertEquals(5, actualOther0[0]);
			assertEquals(6, actualOther0[1]);

			assertEquals(2, actualOther1.length);
			assertEquals(false, actualOther1[0]);
			assertEquals(false, actualOther1[1]);
		}
	}

	@Test
	public void convertTest_multiple()
	{
		List<ChartAttribute> chartAttributes = new ArrayList<>();
		chartAttributes.add(new ChartAttribute("name", ChartAttribute.DataType.STRING, true, true));
		chartAttributes.add(new ChartAttribute("size", ChartAttribute.DataType.NUMBER, true, true));
		chartAttributes.add(new ChartAttribute("enable", ChartAttribute.DataType.BOOLEAN, true, true));
		
		ChartAttributeValueConverter converter = new ChartAttributeValueConverter();
		
		{
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", "aaa");
			values.put("size", "3");
			values.put("enable", "true");
			values.put("other0", new int[] {5, 6});
			values.put("other1", new boolean[] {false, true});

			Map<String, Object> actual = converter.convert(values, chartAttributes);

			Object[] actualName = (Object[])actual.get("name");
			Object[] actualSize = (Object[])actual.get("size");
			Object[] actualEnable = (Object[])actual.get("enable");
			int[] actualOther0 = (int[])actual.get("other0");
			boolean[] actualOther1 = (boolean[])actual.get("other1");

			assertEquals(1, actualName.length);
			assertEquals("aaa", actualName[0]);

			assertEquals(1, actualSize.length);
			assertEquals(3, ((Integer) actualSize[0]).intValue());

			assertEquals(1, actualEnable.length);
			assertEquals(true, ((Boolean) actualEnable[0]).booleanValue());

			assertEquals(2, actualOther0.length);
			assertEquals(5, actualOther0[0]);
			assertEquals(6, actualOther0[1]);

			assertEquals(2, actualOther1.length);
			assertEquals(false, actualOther1[0]);
			assertEquals(true, actualOther1[1]);
		}
	}
}
