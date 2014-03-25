package us.kbase.common.performance.sortjson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nocrala.tools.texttablefmt.Table;

import us.kbase.common.performance.PerformanceMeasurement;
import us.kbase.common.utils.sortjson.SortedKeysJsonFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MeasureSortJsonSpeed {
	
	private static final ObjectMapper SORT_MAPPER = new ObjectMapper();
	static {
		SORT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}

	public static void main(String[] args) throws Exception {
		File f = new File("src/us/kbase/common/performance/sortjson/83333.2.txt");
		int sorts = 100;

		JsonNode jn = new ObjectMapper().readTree(f);
		byte[] b = new ObjectMapper().writeValueAsBytes(jn);
		PerformanceMeasurement js = measureJsonSort(b, sorts);
		PerformanceMeasurement skfj = measureSKJFSort(b, sorts);
		renderResults(Arrays.asList(js, skfj));
		
	}
	
	private static void renderResults(List<PerformanceMeasurement> pms) {
		final int width = 4;
		Table tbl = new Table(width);
		tbl.addCell("Operation");
		tbl.addCell("N");
		tbl.addCell("Mean time (s)");
		tbl.addCell("Std dev (s)");
		for (PerformanceMeasurement pm: pms) {
			tbl.addCell(pm.getName());
			tbl.addCell("" + pm.getN());
			tbl.addCell(String.format("%,.4f", pm.getAverageInSec()));
			tbl.addCell(String.format("%,.4f", pm.getStdDevInSec()));
		}
		System.out.println(tbl.render());
	}

	private static PerformanceMeasurement measureSKJFSort(byte[] b, int sorts)
			throws Exception {
		List<Long> m = new LinkedList<Long>();
		for (int i = 0; i < sorts; i++) {
			long start = System.nanoTime();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			new SortedKeysJsonFile(b).writeIntoStream(baos);
			@SuppressWarnings("unused")
			byte[] t = baos.toByteArray();
			m.add(System.nanoTime() - start);
		}
		return new PerformanceMeasurement(m, "SortedKeysJsonFile JSON sort");
	}

	private static PerformanceMeasurement measureJsonSort(byte[] b, int sorts)
			throws Exception {
		List<Long> m = new LinkedList<Long>();
		for (int i = 0; i < sorts; i++) {
			long start = System.nanoTime();
			@SuppressWarnings("unchecked")
			Map<String, Object> d = SORT_MAPPER.readValue(b, Map.class);
			@SuppressWarnings("unused")
			byte[] t = SORT_MAPPER.writeValueAsBytes(d);
			m.add(System.nanoTime() - start);
		}
		return new PerformanceMeasurement(m, "ObjectMapper JSON sort");
	}
}