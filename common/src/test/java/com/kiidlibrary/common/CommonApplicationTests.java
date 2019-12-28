package com.kiidlibrary.common;

import com.kiidlibrary.common.models.Kiid;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommonApplicationTests {

	@Test
	void contextLoads() {
		Kiid kiid = new Kiid();
		kiid.setAuthor("test");
		kiid.getAuthor();
		kiid.setFileName("test");
		kiid.getFileName();
		kiid.setKiidId("test");
		kiid.getKiidId();
		kiid.setSubject("test");
		kiid.getSubject();
		kiid.setTitle("test");
		kiid.getTitle();
	}

}
