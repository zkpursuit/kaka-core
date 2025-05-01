package com.kaka.notice.detector;

/**
 * 具有优先级的识别器
 *
 * @author zkpursuit
 */
abstract public class PriorityDetector implements IDetector {

    /**
     * 集中处理元素
     */
    protected static class Element<T> {
        private final Object annotation;
        private final Class<T> clasz;

        public Element(Object annotation, Class<T> clasz) {
            this.annotation = annotation;
            this.clasz = clasz;
        }

        public <A> A getAnnotation() {
            return (A) annotation;
        }

        public Class<T> getClasz() {
            return clasz;
        }
    }

    /**
     * 集中化处理
     */
    abstract public void centralizeProcess();
}
