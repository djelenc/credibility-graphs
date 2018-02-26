# Trust model outline

Making a simple trust model from belief revision operators.

## Experiences

* Aggregate all experiences and form total order; this is the **starting knowledge-base.**
    * Use pairwise comparisons to form a total order from all experiences (although total order is not required);
    * Interpret experience as the most reliable reporter;
        * Future work: Sort experiences by their confidence (experiences that are recent and plenty bare more confidence than old and few)

## Opinions

* Construct a knowledge-base for every reporter based on the opinions that it provides:
    * The knowledge-base is assumed to be internally consistent;
    * Use total order like with experiences

## Trust

* Combine experience knowledge-base with opinion knowledge-bases
* Merge ever reporter's opinion knowledge-base into the starting knowledge-base

## Summary

* In ever tick, create a starting knowledge base from all experiences gathered so far
    * Optionally, include the previous knowledge-base
* Merge the knowledge-base with received opinion knowledge-bases
* Produce the final knowledge-base and submit it for evaluation.