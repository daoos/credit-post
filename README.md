授信文本报告分析
================

![](https://img.shields.io/badge/version-v1.0-black.svg)![](https://img.shields.io/badge/dropwizard-v1.0-519dd9.svg)

授信文本报告分析Cmb提供了一套用Java编写的文本识别API。它可以采用银行贷后审批报告文本输入，给出文本中出现的相关贷后要求以及风险点。目前仅支持中文的分析。

#### Build Instructions

Several times a year we distribute a new version of the software, which corresponds to a stable commit.

During the time between releases, one can always use the latest, under development version of our code.

Here are some helpful instructions to use the latest code:

#### build with Maven

1. Make sure you have Maven installed, details here: [https://maven.apache.org/](https://maven.apache.org/)
2. If you run this command in the credit-post directory: `mvn package` , it should run the tests and build this jar file: `cmb/target/cmb-1.0-SNAPSHOT.jar`
3. When using the latest version of the code make sure to download the latest versions of the [cmbCom.crfpp](http://218.77.58.165:9080/owncloud/remote.php/webdav/%E6%8B%9B%E8%A1%8C%E6%A8%A1%E5%9E%8Bv1.0/cmbCom.crfpp), [cmbSenten.crfpp](http://218.77.58.165:9080/owncloud/remote.php/webdav/%E6%8B%9B%E8%A1%8C%E6%A8%A1%E5%9E%8Bv1.0/cmbSenten.crfpp), and [ruleFile](http://218.77.58.165:9080/owncloud/remote.php/webdav/%E6%8B%9B%E8%A1%8C%E6%A8%A1%E5%9E%8Bv1.0/ruleFile) and include them in your cmb/model directory.

You can find releases of Cmb on [Gitlab credit-post](http://h133:11000/data-mining-group/credit-post).

You can find more explanation and documentation on [the docs homepage](http://h133:11000/data-mining-group/credit-post/tree/master/docs).

Questions about Cmb can either be posted on issues with the tag [Cmb](http://h133:11000/data-mining-group/credit-post/issues).
