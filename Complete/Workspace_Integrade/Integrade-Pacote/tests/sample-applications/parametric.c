/*
 * Simple parametric test 
 */

#include <stdio.h>

int main(int argc, char *argv[]) {
   
   int fileContent = 0;
   int i = atoi(argv[1]);
   FILE *file = fopen(argv[2], "r");
   
   fscanf(file, "%d", &fileContent);
   fileContent += i;
   fclose(file);

   printf("N:%d\n", fileContent);

   return 0;
}
