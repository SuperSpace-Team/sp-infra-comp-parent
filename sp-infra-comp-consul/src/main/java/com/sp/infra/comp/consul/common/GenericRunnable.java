package com.sp.infra.comp.consul.common;

import com.sp.infra.comp.consul.log.LogType;
import com.sp.infra.comp.logger.log.CompLogger;

/**
 * @Description
 * @Author alexlu
 * @date 2021.08.13
 */
public abstract class GenericRunnable<T> implements Runnable {

    private String key;
    private String name;
    private String lastSource;
    private Watcher<String, String> watcher;
    private Converter<String, T> converter;
    private Dispatcher<T> dispatcher;

    public GenericRunnable(
            String key,
            String name,
            Watcher<String, String> watcher,
            Converter<String, T> converter,
            Dispatcher<T> dispatcher) {
        this.key = key;
        this.name = name;
        this.watcher = watcher;
        this.converter = converter;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        String source = watcher.watch(key);

        if (source == null) {
            return;
        }

        if (source != null && !source.equals(lastSource)) {
            CompLogger.debugx(LogType.LOG_TAG, "The {} Watcher Pulled : {} ", name, source);

            // 更新
            T convertSource = converter.convert(source);
            dispatcher.doDispatch(convertSource);
            lastSource = source;
        }
    }

    public interface Watcher<S, T> {
        T watch(S key);
    }

    public interface Converter<S, T> {
        T convert(S source);
    }

    public interface Dispatcher<S> {
        void doDispatch(S cvrtedSource);
    }

}