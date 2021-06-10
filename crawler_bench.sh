#!/bin/sh
echo "Benchmark started"
for i in 2 4 8 16
do
        ./run.sh pc.crawler.WebServer > /dev/null &
        echo "Using $i crawler threads"
        /bin/time ./run.sh pc.crawler.ConcurrentCrawler $i && killall java &
        wait
done
echo "Benchmark finished."
