package com.yunlinker.model;

/**
 * Created by Administrator on 2018/12/27/027.
 */

public class OSStokensingmodel {

    /**
     * code : 1
     * curPage : 0
     * data : {"status":"200","AccessKeyId":"STS.NJfbqj3mQyuoF2xHkGtw7hptd","AccessKeySecret":"8NwbjyR6iVjAXNiSJpSVdAG9NY3Jr5ZPDNB6Ad8viFxh","SecurityToken":"CAIShwJ1q6Ft5B2yfSjIr4nTKcve3rJwzreERBTJrGsSePgbh7Xfhjz2IHtPfXlpCe8YsPg2lGxW7/8ZlqQqFsMYFRydMJQpvs4LqcNclwxX/57b16cNrbH4M0rxYkeJ8a2/SuH9S8ynCZXJQlvYlyh17KLnfDG5JTKMOoGIjpgVBbZ+HHPPD1x8CcxROxFppeIDKHLVLozNCBPxhXfKB0ca0WgVy0EHsPzhnpLAtkaG0Q2jmrZJ/b6ceMb0M5NeW75kSMqw0eBMca7M7TVd8RAi9t0t1/cdqW6X4Y/DXwABv0vfarHOgdRrLR5kYK8hALJDr/X6mvB+t/bai4Pt0RFJMPGjjZs3vmQ6qRqAAYfi7MznHsleY0gTewS7cpTMw7iLMOLFsuKAU5WY4oTqGL2Iz6XiVHL7I6ZHyeI/ZyBkKYhu73QA30N6Wegu97jcgGbwyfFHfBbgGhupsBnSxwQXXTTFPC8rtbv6Xh++O7Rvzm7umMn2IgBo5qB/k1vtVPpcMbToStielGct0qqj","Expiration":"2018-12-27T03:48:23Z","dir":"2018-12","filename":"1545881572122"}
     * msg :
     * pageSize : 0
     * totalCount : 0
     */

    private int code;
    private int curPage;
    private DataBean data;
    private String msg;
    private int pageSize;
    private int totalCount;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public static class DataBean {
        /**
         * status : 200
         * AccessKeyId : STS.NJfbqj3mQyuoF2xHkGtw7hptd
         * AccessKeySecret : 8NwbjyR6iVjAXNiSJpSVdAG9NY3Jr5ZPDNB6Ad8viFxh
         * SecurityToken : CAIShwJ1q6Ft5B2yfSjIr4nTKcve3rJwzreERBTJrGsSePgbh7Xfhjz2IHtPfXlpCe8YsPg2lGxW7/8ZlqQqFsMYFRydMJQpvs4LqcNclwxX/57b16cNrbH4M0rxYkeJ8a2/SuH9S8ynCZXJQlvYlyh17KLnfDG5JTKMOoGIjpgVBbZ+HHPPD1x8CcxROxFppeIDKHLVLozNCBPxhXfKB0ca0WgVy0EHsPzhnpLAtkaG0Q2jmrZJ/b6ceMb0M5NeW75kSMqw0eBMca7M7TVd8RAi9t0t1/cdqW6X4Y/DXwABv0vfarHOgdRrLR5kYK8hALJDr/X6mvB+t/bai4Pt0RFJMPGjjZs3vmQ6qRqAAYfi7MznHsleY0gTewS7cpTMw7iLMOLFsuKAU5WY4oTqGL2Iz6XiVHL7I6ZHyeI/ZyBkKYhu73QA30N6Wegu97jcgGbwyfFHfBbgGhupsBnSxwQXXTTFPC8rtbv6Xh++O7Rvzm7umMn2IgBo5qB/k1vtVPpcMbToStielGct0qqj
         * Expiration : 2018-12-27T03:48:23Z
         * dir : 2018-12
         * filename : 1545881572122
         */

        private String status;
        private String AccessKeyId;
        private String AccessKeySecret;
        private String SecurityToken;
        private String Expiration;
        private String dir;
        private String filename;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAccessKeyId() {
            return AccessKeyId;
        }

        public void setAccessKeyId(String AccessKeyId) {
            this.AccessKeyId = AccessKeyId;
        }

        public String getAccessKeySecret() {
            return AccessKeySecret;
        }

        public void setAccessKeySecret(String AccessKeySecret) {
            this.AccessKeySecret = AccessKeySecret;
        }

        public String getSecurityToken() {
            return SecurityToken;
        }

        public void setSecurityToken(String SecurityToken) {
            this.SecurityToken = SecurityToken;
        }

        public String getExpiration() {
            return Expiration;
        }

        public void setExpiration(String Expiration) {
            this.Expiration = Expiration;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}
