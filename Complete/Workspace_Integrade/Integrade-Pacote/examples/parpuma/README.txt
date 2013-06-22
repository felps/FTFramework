Parallel PUMA is a parallel version of PUMA that works with computers with one or more processors (or cores) or with a cluster/grid of computers. It uses the message passing parallel programming model enabling the work to be done in parallel among the avaliable resources.
PUMA was originally developed by E. G. Birgin, I. Chambouleyron, J. M. Martinez and S. D. Ventura.
Here you will find information about how to compile and use the
Parallel PUMA. For more information about PUMA and its usage please go
to www.ime.usp.br/~egbirgin/puma/.

The Parallel PUMA is developed entirely in C. 
We also used an implementation of the MPI API to enable parallel programming.

To download the latest version of the Parallel PUMA application, go
 to www.ime.usp.br/~egbirgin/puma/ .
Compile the source code with: mpicc -o parpuma *.c.

The program usage is almost identical to it's original version. If you
are not familiar with it, please read the user guide available at PUMA
website (www.ime.usp.br/~egbirgin/puma/).
