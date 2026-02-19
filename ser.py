import numpy as np
import os

print("=== Конвертер полярных координат в декартовы ===")

input_file = 'data.csv'
output_file = 'output.xyz'

if not os.path.exists(input_file):
    print(f"Ошибка: Файл {input_file} не найден!")
    exit()

points = []
error_count = 0

with open(input_file, 'r') as f:
    for line_num, line in enumerate(f, 1):
        line = line.strip()
        
        if not line:
            continue
            
        if any(c.isalpha() for c in line):
            print(f"Пропущена строка {line_num}: содержит текст - '{line}'")
            continue
            
        if ';' in line:
            parts = line.split(';')
        else:
            parts = line.replace(',', ';').split(';')
            
        if len(parts) >= 3:
            try:
                phi_str = parts[0].strip()
                theta_str = parts[1].strip()
                r_str = parts[2].strip()
                
                if not phi_str.replace('.', '').replace('-', '').isdigit():
                    print(f"Пропущена строка {line_num}: phi не число - '{phi_str}'")
                    continue
                if not theta_str.replace('.', '').replace('-', '').isdigit():
                    print(f"Пропущена строка {line_num}: theta не число - '{theta_str}'")
                    continue
                if not r_str.replace('.', '').replace('-', '').isdigit():
                    print(f"Пропущена строка {line_num}: r не число - '{r_str}'")
                    continue
                
                phi = float(phi_str)
                theta = float(theta_str)
                r = float(r_str)
                
                phi_rad = phi * np.pi / 180.0
                theta_rad = theta * np.pi / 180.0
                
                x = r * np.sin(theta_rad) * np.cos(phi_rad)
                y = r * np.sin(theta_rad) * np.sin(phi_rad)
                z = r * np.cos(theta_rad)
                
                points.append((x, y, z, r, phi, theta))
                
            except ValueError as e:
                error_count += 1
                print(f"Ошибка в строке {line_num}: {e} - '{line}'")
                continue
            except Exception as e:
                error_count += 1
                print(f"Неизвестная ошибка в строке {line_num}: {e}")
                continue
        else:
            print(f"Пропущена строка {line_num}: недостаточно данных - '{line}'")

print(f"\nОбработано строк: {line_num}")
print(f"Успешно конвертировано: {len(points)} точек")
print(f"Ошибок: {error_count}")

if len(points) == 0:
    print("Нет данных для сохранения!")
    exit()

print("\n1. Сохраняю в формате XYZ...")
with open('output.xyz', 'w') as f:
    f.write(f"{len(points)}\n")
    f.write("Converted from polar coordinates\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"C {x:.6f} {y:.6f} {z:.6f} {r:.6f}\n")
print(f"✓ Создан файл: output.xyz")

print("2. Сохраняю в формате CSV...")
with open('output.csv', 'w') as f:
    f.write("X,Y,Z,Radius,Phi,Theta\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"{x:.6f},{y:.6f},{z:.6f},{r:.6f},{phi:.6f},{theta:.6f}\n")
print(f"✓ Создан файл: output.csv")

print("3. Сохраняю в формате VTK...")
with open('output.vtk', 'w') as f:
    f.write("# vtk DataFile Version 3.0\n")
    f.write("Polar to Cartesian Conversion\n")
    f.write("ASCII\n")
    f.write("DATASET POLYDATA\n")
    f.write(f"POINTS {len(points)} float\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"{x:.6f} {y:.6f} {z:.6f}\n")
    f.write(f"VERTICES {len(points)} {len(points)*2}\n")
    for i in range(len(points)):
        f.write(f"1 {i}\n")
    f.write(f"POINT_DATA {len(points)}\n")
    f.write(f"SCALARS Radius float 1\n")
    f.write("LOOKUP_TABLE default\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"{r:.6f}\n")
    f.write(f"SCALARS Phi float 1\n")
    f.write("LOOKUP_TABLE default\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"{phi:.6f}\n")
    f.write(f"SCALARS Theta float 1\n")
    f.write("LOOKUP_TABLE default\n")
    for x, y, z, r, phi, theta in points:
        f.write(f"{theta:.6f}\n")
print(f"✓ Создан файл: output.vtk")

print("4. Сохраняю в простом формате...")
with open('output.txt', 'w') as f:
    for x, y, z, r, phi, theta in points:
        f.write(f"{x} {y} {z} {r}\n")
print(f"✓ Создан файл: output.txt")

print("\n=== СТАТИСТИКА ===")
x_vals = [p[0] for p in points]
y_vals = [p[1] for p in points]
z_vals = [p[2] for p in points]
r_vals = [p[3] for p in points]

print(f"Координаты X: {min(x_vals):.2f} ... {max(x_vals):.2f}")
print(f"Координаты Y: {min(y_vals):.2f} ... {max(y_vals):.2f}")
print(f"Координаты Z: {min(z_vals):.2f} ... {max(z_vals):.2f}")
print(f"Радиусы: {min(r_vals):.2f} ... {max(r_vals):.2f}")

print("\n=== КАК ИСПОЛЬЗОВАТЬ В PARAVIEW ===")
print("Способ 1 (рекомендуется):")
print("  1. Откройте ParaView")
print("  2. File → Open → выберите output.vtk")
print("  3. Нажмите Apply")
print("  4. Готово! Точки будут с цветом по радиусу")

print("\nСпособ 2:")
print("  1. Откройте ParaView")
print("  2. File → Open → выберите output.csv")
print("  3. В 'Open Data With...' выберите CSV Reader")
print("  4. Нажмите Apply")
print("  5. Filters → Alphabetical → Table To Points")
print("  6. Установите: X Column = X, Y Column = Y, Z Column = Z")
print("  7. Нажмите Apply")

print("\nСпособ 3:")
print("  1. Откройте ParaView")
print("  2. File → Open → выберите output.xyz")
print("  3. В 'Open Data With...' выберите XYZ Reader")
print("  4. Нажмите Apply")

print("\n✓ Конвертация завершена успешно!")

