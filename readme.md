# TransX

知识表示Trans系列之TransE、TransH 以及PTransE算法

### 代码主要参考清华NLP实验室的代码内容

- 源码来自[KE2G](https://github.com/thunlp/KB2E)，我将其转换成Java形式
- 由于THUNLP的源码对中文处理不太友好，如在处理中文数据集过程中无法输出中间的信息如Mean-Rank的内容，我自己根据需要将其输出，以便测试代码结果或者对比结果。
- 实验使用的中文数据源自网上一份[三国演义人物关系图](http://huaban.com/pins/50364904/)，对其进行了整理得到相应的三元组。
- 实验的效果根据自己需要进行相应的调参，主要的参数放在GlobalValue.java 文件中。从Mean-Rank和Hits@10的指标来看，该份数据集大部分情况下效果为：TransH > PTransE > TransE
- 代码复现过程中，我根据自己的理解对源C++代码进行了部分修改，剔除了一些冗余和不合理的代码：
  1.  Mean-Rank和Hits@10采用了改进的版本，在评测未知关系三元组过程中，对已知正确的三元组在评测过程剔除。关于TransE评测指标更详细的介绍可参考这篇文章 [TransE评测方法](https://xiangrongzeng.github.io/knowledge%20graph/transE-evaluation.html)
  2.  删除C++版本的一些冗余参数
  3. PTransE中关于路径的计算，没有计算了源代码反向的路径，如$(A, relation, B) -> (B, relation^{-1}, A)$ ，以及PTransE_Test沿用TransE的代码，与TransE评测一致

### 代码的原理来源于几篇TransE相关的论文

1. [TransE](https://papers.nips.cc/paper/5071-translating-embeddings-for-modeling-multi-relational-data.pd)
2. [TransH](https://pdfs.semanticscholar.org/2a3f/862199883ceff5e3c74126f0c80770653e05.pdf)
3. [PTransE](https://arxiv.org/pdf/1506.00379.pdf)

