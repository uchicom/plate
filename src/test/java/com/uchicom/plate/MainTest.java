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
  @Mock com.uchicom.plate.service.DeployService deployService;
  @Mock com.uchicom.plate.service.GithubService githubService;
  @Mock java.util.logging.Logger logger;

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
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).from)
        .isEqualTo("glob:test1.jar");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).decompress).isNull();
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(0).to).isEqualTo("hoge/lib/");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).from)
        .isEqualTo("glob:test1.zip");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).decompress)
        .isEqualTo("glob:**test.htm");
    assertThat(actual.release.get("hoge").deploy.deployFiles.get(1).to).isEqualTo("hoge/www/");
  }

  @Test
  public void shutdownWithoutConfig() {
    var starter = org.mockito.Mockito.mock(Starter.class);
    org.mockito.Mockito.when(starter.isFinish()).thenReturn(false, true);
    main.addKey("key1", "com.uchicom.hoge.Main", "service");
    main.getPortMap().get("service").getList().get(0).getStarterList().add(starter);

    main.shutdown();

    org.mockito.Mockito.verify(starter).shutdown();
  }

  @Test
  public void shutdownWithoutPort() {
    main.shutdown();
  }

  @Test
  public void starterFinishesAfterRun() {
    main.addKey("key1", "com.uchicom.plate.NoSuchClass", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    var starter = keyInfo.create(new String[0], Starter.StarterKind.CALL);
    starter.setStarted(true);
    assertThat(starter.isAlive()).isTrue();

    starter.run();

    assertThat(starter.isFinish()).isTrue();
    assertThat(starter.isAlive()).isFalse();
    main.purge();
    assertThat(keyInfo.getStarterList()).isEmpty();
  }

  @Test
  public void shutdownBeforeClassLoadedOrWithoutShutdownMethod() {
    main.addKey("key1", "com.uchicom.plate.NoSuchClass", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    var starter = keyInfo.create(new String[0], Starter.StarterKind.SERVICE);
    starter.setStarted(true);
    keyInfo.shutdownMethodName = "shutdown";

    starter.shutdown();

    assertThat(starter.isFinish()).isTrue();

    var starter2 = keyInfo.create(new String[0], Starter.StarterKind.SERVICE);
    starter2.setStarted(true);
    keyInfo.shutdownMethodName = null;
    starter2.shutdown(new String[] {"a"});
    assertThat(starter2.isFinish()).isTrue();
  }

  @Test
  public void buildClosesOldClassLoader() throws Exception {
    main.addKey("key1", "com.uchicom.plate.Main", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    var cp = new CpInfo(new File("target/classes/").toURI().toString());
    cp.setStatus(com.uchicom.plate.enumeration.CpState.INCLUDED);
    keyInfo.addCp(cp);
    keyInfo.build();
    var first = keyInfo.getClassLoader();
    assertThat(first).isNotNull();

    keyInfo.build();

    assertThat(keyInfo.getClassLoader()).isNotSameAs(first);
    // close済みのURLClassLoaderは新規リソース取得ができない
    assertThat(first.findResource("com/uchicom/plate/Main.class")).isNull();
  }

  @Test
  public void removeKeyStopsStarterAndSchedule() throws Exception {
    var starter = org.mockito.Mockito.mock(Starter.class);
    org.mockito.Mockito.when(starter.isFinish()).thenReturn(false, true);
    var schedule = org.mockito.Mockito.mock(com.uchicom.plate.scheduler.Schedule.class);
    main.addKey("key1", "com.uchicom.hoge.Main", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    keyInfo.getStarterList().add(starter);
    keyInfo.setSchedule(schedule);

    main.removeKey("key1", "service");

    org.mockito.Mockito.verify(starter).shutdown();
    org.mockito.Mockito.verify(schedule).cancel();
    assertThat(main.getPortMap().get("service").getList()).isEmpty();
  }

  @Test
  public void restartKeyIgnoresRecovery() throws Exception {
    main.addKey("key1", "com.uchicom.plate.NoSuchClass", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    keyInfo.setStatus(com.uchicom.plate.enumeration.KeyState.ENABLE);
    var old = org.mockito.Mockito.mock(Starter.class);
    org.mockito.Mockito.when(old.isFinish()).thenReturn(false, true);
    keyInfo.getStarterList().add(old);

    main.restartKey("service", "key1");

    org.mockito.Mockito.verify(old).shutdown();
    assertThat(keyInfo.getStarterList()).hasSize(1);
    assertThat(keyInfo.getStarterList().get(0)).isNotSameAs(old);
  }

  @Test
  public void releaseRenamesOnlyOnDownloadOrDeployFailure(
      @org.junit.jupiter.api.io.TempDir File dir) throws Exception {
    var releaseDto = new com.uchicom.plate.dto.ReleaseDto();
    releaseDto.dirPath = dir.getPath();
    releaseDto.auto = new com.uchicom.plate.dto.AutoDto();
    releaseDto.auto.detect = "^(v[0-9]+)[.]txt$";
    var file = new File(dir, "v1.txt");
    org.assertj.core.api.Assertions.assertThat(file.createNewFile()).isTrue();
    org.mockito.Mockito.doThrow(new com.uchicom.plate.exception.ServiceException("ng"))
        .when(deployService)
        .deploy(releaseDto, "v1");

    main.release(dir.getPath(), java.nio.file.Path.of("v1.txt"), java.util.List.of(releaseDto));

    assertThat(file).doesNotExist();
    assertThat(new File(dir, "ERR_v1.txt")).exists();
  }

  private Starter newService(String... params) {
    FixtureService.shutdownCount = 0;
    FixtureService.failShutdown = false;
    main.addKey("fx", "com.uchicom.plate.FixtureService", "service");
    var keyInfo = main.getPortMap().get("service").getList().get(0);
    keyInfo.shutdownMethodName = "shutdown";
    keyInfo.setStatus(com.uchicom.plate.enumeration.KeyState.ENABLE);
    var starter = keyInfo.create(params, Starter.StarterKind.SERVICE);
    starter.setStarted(true);
    return starter;
  }

  @Test
  public void serviceStaysAliveAfterMainReturnsAndShutdownFinishes() {
    var starter = newService();

    starter.run();

    assertThat(starter.isFinish()).isFalse();
    assertThat(starter.isAlive()).isTrue();
    starter.shutdown();
    assertThat(FixtureService.shutdownCount).isEqualTo(1);
    assertThat(starter.isFinish()).isTrue();
  }

  @Test
  public void mainShutdownCallsShutdownMethodOfRunningService() {
    newService().run();

    main.shutdown();

    assertThat(FixtureService.shutdownCount).isEqualTo(1);
  }

  @Test
  public void restartKeyCallsShutdownMethod() throws Exception {
    newService().run();

    main.restartKey("service", "fx");

    assertThat(FixtureService.shutdownCount).isEqualTo(1);
    assertThat(main.getPortMap().get("service").getList().get(0).getStarterList()).hasSize(1);
  }

  @Test
  public void restartKeyDoesNotRestartWhenShutdownFails() {
    var starter = newService();
    starter.run();
    FixtureService.failShutdown = true;

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> main.restartKey("service", "fx"))
        .isInstanceOf(com.uchicom.plate.exception.CmdException.class);

    var list = main.getPortMap().get("service").getList().get(0).getStarterList();
    assertThat(list).containsExactly(starter);
    assertThat(starter.isFinish()).isFalse();
  }

  @Test
  public void serviceWithoutShutdownMethodFinishesWhenMainReturns() {
    var starter = newService();
    main.getPortMap().get("service").getList().get(0).shutdownMethodName = null;

    starter.run();

    assertThat(starter.isFinish()).isTrue();
  }

  @Test
  public void serviceFinishesWhenMainThrows() {
    var starter = newService("error");

    starter.run();

    assertThat(starter.isFinish()).isTrue();
  }

  @Test
  public void serviceDoesNotClearLogAndDriverRightAfterMain() {
    var starter = org.mockito.Mockito.spy(newService());

    starter.run();

    org.mockito.Mockito.verify(starter, org.mockito.Mockito.never())
        .clearLog(org.mockito.ArgumentMatchers.any());
    org.mockito.Mockito.verify(starter, org.mockito.Mockito.never())
        .clearDriver(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void validateDefaultsOrderAndMethod() {
    var service = new com.uchicom.plate.dto.ServicesDto();
    service.key = "s";
    service.className = "a.B";
    service.order = null;
    service.method = null;
    var config = new PlateConfig();
    config.service = new com.uchicom.plate.dto.ServiceDto();
    config.service.services = java.util.List.of(service);

    main.validate(config);

    assertThat(service.order).isNotNull();
    assertThat(service.method.startup).isEqualTo("main");
  }

  @Test
  public void validateInvalidCronThrows() {
    var batch = new com.uchicom.plate.dto.BatchesDto();
    batch.key = "b";
    batch.className = "a.B";
    batch.schedule = new com.uchicom.plate.dto.ScheduleDto();
    batch.schedule.cron = "bad";
    org.mockito.Mockito.when(scheduleFactory.create("bad"))
        .thenThrow(new IllegalArgumentException("bad cron"));
    var config = new PlateConfig();
    config.batch = new com.uchicom.plate.dto.BatchDto();
    config.batch.batches = java.util.List.of(batch);

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> main.validate(config))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void stopAll_closesClassLoaders() throws Exception {
    main.addKey("k", "a.B", "main", "p");
    var porter = main.getPortMap().get("p");
    var keyInfo = porter.getList().get(0);
    var cpInfo = new CpInfo(new File("a.jar").toURI().toString());
    porter.addCp(cpInfo);
    keyInfo.addCp(cpInfo);
    porter.build();
    var porterLoader = porter.getClassLoader();
    var keyLoader = keyInfo.getClassLoader();
    assertThat(porterLoader).isNotNull();
    assertThat(keyLoader).isNotNull();

    main.stopAll();

    assertThat(porter.getClassLoader()).isNull();
    assertThat(keyInfo.getClassLoader()).isNull();
    assertThat(main.getPortMap()).isEmpty();
  }
}
