#!/bin/sh
echo "Setup Paper NMS for version 1.20.5..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.20.5 -P Platform.Paper

echo "Setup Paper NMS for version 1.21..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.2..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.2 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.4..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.4 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.5..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.5 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.6..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.6 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.9..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.9 -P Platform.Paper

echo "Setup Paper NMS for version 1.21.11..."
mvn --quiet paper-nms:init -pl :commandapi-paper-1.21.11 -P Platform.Paper

echo "Done!"