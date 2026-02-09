// (C) 2022 uchicom
package com.uchicom.plate;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchicom.plate.dto.PlateConfig;
import com.uchicom.plate.scheduler.ScheduleFactory;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class MainTest extends AbstractTest {
  @Mock ScheduleFactory scheduleFactory;

  @InjectMocks Main main;

  @Test
  public void loadConfig() throws Exception {
    PlateConfig actual = main.loadConfig(new File("./src/test/resources/config.yml"));
    assertThat(actual).isNotNull();
    assertThat(actual.service).isNotNull();
    assertThat(actual.service.classPath).isNull();
    assertThat(actual.service.services).hasSize(1);
    assertThat(actual.service.services.get(0).classPath).isNull();
    assertThat(actual.service.services.get(0).key).isEqualTo("server1");
    assertThat(actual.service.services.get(0).className).isEqualTo("com.uchicom.hoge.Main");
    assertThat(actual.service.services.get(0).method.startup).isEqualTo("main");
    assertThat(actual.service.services.get(0).method.shutdown).isEqualTo("shutdown");
    assertThat(actual.service.services.get(0).parameters).hasSize(4);
    assertThat(actual.service.services.get(0).parameters[0]).isEqualTo("a");
    assertThat(actual.service.services.get(0).parameters[1]).isEqualTo("b");
    assertThat(actual.service.services.get(0).parameters[2]).isEqualTo("c");
    assertThat(actual.service.services.get(0).parameters[3]).isEqualTo("d");
    assertThat(actual.service.services.get(0).disabled).isFalse();
    assertThat(actual.service.services.get(0).recovery).isTrue();
    assertThat(actual.batch).isNotNull();
    assertThat(actual.batch.classPath).isNull();
    assertThat(actual.batch.batches).hasSize(1);
    assertThat(actual.batch.batches.get(0).classPath).isNull();
    assertThat(actual.batch.batches.get(0).schedule.cron).isEqualTo("1 2 3 4 5");
    assertThat(actual.release).isNotNull();
    assertThat(actual.release).hasSize(2);
    assertThat(actual.release.get("hoge").dirPath).isEqualTo("release");
    assertThat(actual.release.get("hoge").github).isNotNull();
    assertThat(actual.release.get("hoge").deploy).isNotNull();
    assertThat(actual.release.get("hoge").deploy.deployFiles).hasSize(2);
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).from).isEqualTo("/*.jar");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).decompress).isNull();
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).to).isEqualTo("hoge/lib/");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).from).isEqualTo("/*.tar.gz");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).decompress)
        .isEqualTo("*/www/*");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).to).isEqualTo("hoge/www/");
  }
}
