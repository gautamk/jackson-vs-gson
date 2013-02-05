package com.gautamk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.gson.Gson;

public class JacksonVsGson {
	
	public static class RandomClass{
		int integer;
		String string;
		double decimal;
		public static final int MAX_STRING_LENGTH = 300;
		
		public int getInteger() {
			return integer;
		}
		public void setInteger(int integer) {
			this.integer = integer;
		}
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public double getDecimal() {
			return decimal;
		}
		public void setDecimal(double decimal) {
			this.decimal = decimal;
		}
		
		@Override
		public String toString() {
			return "RandomClass [integer=" + integer + ", string=" + string
					+ ", decimal=" + decimal + "]";
		}
		private RandomClass(int integer, String string, double decimal) {
			super();
			this.integer = integer;
			this.string = string;
			this.decimal = decimal;
		}
		private static int getRandomNumber(int Min,int Max){
			return Min + (int)(Math.random() * ((Max - Min) + 1));
		}
		private static int getRandomCharCode(){
			int Min = 65;
			int Max = 122;
			return getRandomNumber(Min, Max);
		}
		private static char getRandomChar(){
			return (char)getRandomCharCode();
		}
		private static String getRandomString(){
			StringBuffer buff = new StringBuffer();
			int max = getRandomNumber(1, MAX_STRING_LENGTH);
			for (int i=0;i<=max;i++){
				buff.append(getRandomChar());
			}
			return buff.toString();
		}
		public static RandomClass getRandomInstance(){
			return new RandomClass(
					getRandomNumber(0, MAX_STRING_LENGTH),
					getRandomString(),
					getRandomNumber(0, MAX_STRING_LENGTH)
					);
		}
		public static RandomClass[] getArrayOfRandomInstances(){
			int len = getRandomNumber(1, 100000);
			RandomClass[] randomClasses = new RandomClass[len];
			for(int i =0 ;i<len;i++){
				randomClasses[i] = getRandomInstance();
			}
			return randomClasses;
		}
	}
	private static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

	public static class JacksonVsGsonBenchmark extends SimpleBenchmark {
		@Param({ "10", "100", "1000", "10000","100000" })
		private int length;
		final String string;
		final RandomClass[] randomClasses;
		public JacksonVsGsonBenchmark() throws IOException {
			this.string = readFile("test_data.json");
			randomClasses = RandomClass.getArrayOfRandomInstances();
		}
		public void timeJacksonDe(int reps) throws JsonProcessingException, IOException{
			ObjectMapper mapper = new ObjectMapper();
			for (int i = 0; i < reps; i++) {
				JsonNode rootNode = mapper.readTree(string);
			}
		}
		public void timeJacksonSerialize(int reps)
				throws JsonProcessingException, IOException {
			ObjectMapper mapper = new ObjectMapper();
			for(int i=0; i<reps;i++){
				mapper.writeValueAsString(randomClasses);
			}
		}
		public void timeGsonDeserialize(int reps){
			Gson gson = new Gson();
			for (int i = 0; i < reps; i++) {
				gson.toJson(randomClasses);
			}
		}
		public void timeGsonSerialize(int reps) {
			Gson gson = new Gson();
			for (int i = 0; i < reps; i++) {
				gson.fromJson(string, Object.class);
			}
			
		}
	}

	public static void main(String[] args) throws JsonProcessingException,
			IOException {
		Runner.main(JacksonVsGsonBenchmark.class, args);

	}

}
