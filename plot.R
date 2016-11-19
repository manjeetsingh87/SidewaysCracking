library(ggplot2)

response_times = read.csv('response_times.csv', header = TRUE, sep = ',')
ggplot(data = response_times, aes(x = QuerySequence, y = ResponseTime, color = Type)) + geom_point()

one = read.csv('1.csv', header = TRUE, sep = ',')
one$count <- 1

two = read.csv('2.csv', header = TRUE, sep = ',')
two$count <- 2

three = read.csv('3.csv', header = TRUE, sep = ',')
three$count <- 3

four = read.csv('4.csv', header = TRUE, sep = ',')
four$count <- 4


five = read.csv('5.csv', header = TRUE, sep = ',')
five$count <- 5

six = read.csv('6.csv', header = TRUE, sep = ',')
six$count <- 6

seven = read.csv('7.csv', header = TRUE, sep = ',')
seven$count <- 7

eight = read.csv('8.csv', header = TRUE, sep = ',')
eight$count <- 8

total <- rbind(one, two, three, four, five, six, seven, eight)

ggplot(data = total, aes(x = count)) +  geom_jitter(aes(y = ResponseTime, color = Type))