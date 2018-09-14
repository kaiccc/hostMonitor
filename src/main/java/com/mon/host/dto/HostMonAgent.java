package com.mon.host.dto;

public class HostMonAgent implements Comparable<HostMonAgent>{


    /**
     * hostName : local_os.getHostname()
     * data : {"rx":"receive","tx":"transmit","date":"now"}
     */

    private String hostName;
    private DataBean data;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Override
    public int compareTo(HostMonAgent o) {
        return Long.compare(this.data.getTime(),o.getData().getTime());
    }

    public static class DataBean {
        /**
         * in : Input
         * out : Output
         * date : now
         */

        private double in;
        private double out;
        private long time;

        public double getIn() {
            return in;
        }

        public void setIn(double in) {
            this.in = in;
        }

        public double getOut() {
            return out;
        }

        public void setOut(double out) {
            this.out = out;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}
