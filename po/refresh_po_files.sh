ls -1 ../src/org/openstreetmap/josm/plugins/kindahackedinutils/*.java >files_list
xgettext --files-from=files_list -d kindahackedinutils --from-code=UTF-8 -k -ktrc:1c,2 -kmarktrc:1c,2 -ktr -kmarktr -ktrn:1,2 -ktrnc:1c,2,3

msgmerge -U de.po kindahackedinutils.po
rm kindahackedinutils.po
