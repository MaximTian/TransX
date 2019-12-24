# TransX

知识表示Trans系列之TransE、TransH、TransR以及PTransE算法的JAVA实现

### 代码主要参考清华NLP实验室的代码内容

- 代码大部分内容参考[KE2G](https://github.com/thunlp/KB2E)
- 实验使用的中文数据源自网上一份[三国演义人物关系图](http://huaban.com/pins/50364904/)，对其进行了整理得到相应的三元组。
- 实验的效果根据自己需要进行相应的调参，主要的参数放在GlobalValue.java 文件中。
- 代码复现过程中，我根据自己的理解对源C++代码进行了部分修改，剔除了一些冗余和不合理的代码
- Mean-Rank和Hits@10采用了Raw和Filter两种评价方式。关于TransE评测指标更详细的介绍可参考这篇文章 [TransE评测方法](https://xiangrongzeng.github.io/knowledge%20graph/transE-evaluation.html)

### 代码的原理来源于几篇TransE相关的论文

1. [TransE](https://papers.nips.cc/paper/5071-translating-embeddings-for-modeling-multi-relational-data.pd)
2. [TransH](https://pdfs.semanticscholar.org/2a3f/862199883ceff5e3c74126f0c80770653e05.pdf)
3. [TransR](<https://www.aaai.org/ocs/index.php/AAAI/AAAI15/paper/viewFile/9571/9523/>)
4. [PTransE](https://arxiv.org/pdf/1506.00379.pdf)

### 运行环境

jdk-1.8.0

### 代码结构

- TransH/TransR/TransE/PTransE文件夹

  1. GlobalValue.java：训练参数内容

  2. Gradient.java：梯度下降，以及知识表示的向量更新的过程
  3. Pair.java：自定义的数据结构，实现类似C++中Pair数据结构
  4. TrainPrepare.java/TestPrepare.java：读取训练文件/测试文件
  5. Train.java：训练过程，依次为参数初始化、梯度下降迭代、写实体、关系的表示向量
  6. Test.java：实体链接测试过程，依次为读取向量文件、测试实体链接性能

- Main.java：选择训练还是测试，输入“y”则训练，输入“n”为测试

- resource文件夹

  	1. data文件夹：输入文件，包含实体集合文件entity2id.txt，关系集合文件relation2id.txt，三元组训练、测试文件train.txt和test.txt
   	2. result文件夹：知识表示向量输出文件夹，包含实体向量entity2vec.bern、关系向量relation2vec.bern，变换矩阵Wr_vec.bern(TransH、TransR才拥有)

### 格式说明

entity2id.txt中，输入格式为：

```tex
{实体名称} \t {id}
```

relation2id.txt中，输入格式为：

```tex
{关系名称} \t {id}
```

train.txt\test.txt 格式为：

```tex
{head} \t {tail} \t {relaiton}
```

